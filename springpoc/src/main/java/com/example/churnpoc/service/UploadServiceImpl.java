package com.example.churnpoc.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.churnpoc.dao.CustomerRepository;
import com.example.churnpoc.dao.PredictionResultRepository;
import com.example.churnpoc.dto.UploadReceipt;
import com.example.churnpoc.entity.Customer;

@Service
public class UploadServiceImpl implements UploadService {

    private static final List<String> REQUIRED_COLUMNS = List.of(
            "customerID", "gender", "SeniorCitizen", "Partner", "Dependents",
            "tenure", "PhoneService", "MultipleLines", "InternetService",
            "OnlineSecurity", "OnlineBackup", "DeviceProtection", "TechSupport",
            "StreamingTV", "StreamingMovies", "Contract", "PaperlessBilling",
            "PaymentMethod", "MonthlyCharges", "TotalCharges");

    // must mirror the ML service's Pydantic whitelists, or rows that load here fail there
    private static final Set<String> YES_NO = Set.of("Yes", "No");
    private static final Set<String> GENDERS = Set.of("Male", "Female");
    private static final Set<String> PHONE_ADDON = Set.of("Yes", "No", "No phone service");
    private static final Set<String> INTERNET_TYPES = Set.of("DSL", "Fiber optic", "No");
    private static final Set<String> INTERNET_ADDON = Set.of("Yes", "No", "No internet service");
    private static final Set<String> CONTRACTS = Set.of("Month-to-month", "One year", "Two year");
    private static final Set<String> PAYMENT_METHODS = Set.of(
            "Electronic check", "Mailed check",
            "Bank transfer (automatic)", "Credit card (automatic)");

    private static final int MAX_SAMPLE_ERRORS = 5;

    private CustomerRepository customerRepository;
    private PredictionResultRepository predictionResultRepository;

    @Autowired
    public UploadServiceImpl(CustomerRepository theCustomerRepository,
                             PredictionResultRepository thePredictionResultRepository) {
        customerRepository = theCustomerRepository;
        predictionResultRepository = thePredictionResultRepository;
    }

    @Override
    @Transactional  // delete-old + insert-new is all-or-nothing
    public UploadReceipt load(MultipartFile theFile) throws IOException {

        List<Customer> customers = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();
        int received = 0;
        int skipped = 0;

        try (Reader reader = new InputStreamReader(theFile.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            List<String> missing = REQUIRED_COLUMNS.stream()
                    .filter(col -> !parser.getHeaderNames().contains(col))
                    .toList();
            if (!missing.isEmpty()) {
                throw new IllegalArgumentException("CSV is missing required columns: " + missing);
            }

            for (CSVRecord record : parser) {
                received++;
                try {
                    Customer customer = toCustomer(record);
                    if (!seenIds.add(customer.getExternalId())) {
                        throw new IllegalArgumentException("duplicate customerID");
                    }
                    customers.add(customer);
                }
                catch (Exception exc) {
                    skipped++;
                    if (errors.size() < MAX_SAMPLE_ERRORS) {
                        errors.add("Row " + record.getRecordNumber()
                                + " (id " + record.get("customerID") + "): " + exc.getMessage());
                    }
                }
            }
        }

        // a new upload replaces the previous customer set (predictions go first: FK)
        predictionResultRepository.deleteAllInBatch();
        customerRepository.deleteAllInBatch();
        customerRepository.saveAll(customers);

        return new UploadReceipt(received, customers.size(), skipped, errors);
    }

    private Customer toCustomer(CSVRecord record) {
        Customer customer = new Customer();
        customer.setExternalId(requireText(record.get("customerID"), "customerID"));
        customer.setGender(oneOf(record.get("gender"), GENDERS, "gender"));
        customer.setSeniorCitizen(intInRange(record.get("SeniorCitizen"), "SeniorCitizen", 0, 1));
        customer.setPartner(oneOf(record.get("Partner"), YES_NO, "Partner"));
        customer.setDependents(oneOf(record.get("Dependents"), YES_NO, "Dependents"));
        customer.setTenure(intInRange(record.get("tenure"), "tenure", 0, 120));
        customer.setPhoneService(oneOf(record.get("PhoneService"), YES_NO, "PhoneService"));
        customer.setMultipleLines(oneOf(record.get("MultipleLines"), PHONE_ADDON, "MultipleLines"));
        customer.setInternetService(oneOf(record.get("InternetService"), INTERNET_TYPES, "InternetService"));
        customer.setOnlineSecurity(oneOf(record.get("OnlineSecurity"), INTERNET_ADDON, "OnlineSecurity"));
        customer.setOnlineBackup(oneOf(record.get("OnlineBackup"), INTERNET_ADDON, "OnlineBackup"));
        customer.setDeviceProtection(oneOf(record.get("DeviceProtection"), INTERNET_ADDON, "DeviceProtection"));
        customer.setTechSupport(oneOf(record.get("TechSupport"), INTERNET_ADDON, "TechSupport"));
        customer.setStreamingTv(oneOf(record.get("StreamingTV"), INTERNET_ADDON, "StreamingTV"));
        customer.setStreamingMovies(oneOf(record.get("StreamingMovies"), INTERNET_ADDON, "StreamingMovies"));
        customer.setContract(oneOf(record.get("Contract"), CONTRACTS, "Contract"));
        customer.setPaperlessBilling(oneOf(record.get("PaperlessBilling"), YES_NO, "PaperlessBilling"));
        customer.setPaymentMethod(oneOf(record.get("PaymentMethod"), PAYMENT_METHODS, "PaymentMethod"));
        customer.setMonthlyCharges(doubleInRange(record.get("MonthlyCharges"), "MonthlyCharges", 0, 500));
        customer.setTotalCharges(doubleInRange(record.get("TotalCharges"), "TotalCharges", 0, 60000));
        return customer;
    }

    private String requireText(String value, String column) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("blank " + column);
        }
        return value;
    }

    private String oneOf(String value, Set<String> allowed, String column) {
        requireText(value, column);
        if (!allowed.contains(value)) {
            throw new IllegalArgumentException(
                    "invalid " + column + " \"" + value + "\" (allowed: " + allowed + ")");
        }
        return value;
    }

    private int intInRange(String value, String column, int min, int max) {
        requireText(value, column);
        int parsed;
        try {
            parsed = Integer.parseInt(value);
        }
        catch (NumberFormatException exc) {
            throw new IllegalArgumentException("non-numeric " + column + " \"" + value + "\"");
        }
        if (parsed < min || parsed > max) {
            throw new IllegalArgumentException(
                    column + " " + parsed + " outside allowed range " + min + "-" + max);
        }
        return parsed;
    }

    private double doubleInRange(String value, String column, double min, double max) {
        requireText(value, column);
        double parsed;
        try {
            parsed = Double.parseDouble(value);
        }
        catch (NumberFormatException exc) {
            throw new IllegalArgumentException("non-numeric " + column + " \"" + value + "\"");
        }
        if (parsed < min || parsed > max) {
            throw new IllegalArgumentException(
                    column + " " + parsed + " outside allowed range " + min + "-" + max);
        }
        return parsed;
    }
}
