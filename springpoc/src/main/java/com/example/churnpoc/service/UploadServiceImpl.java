package com.example.churnpoc.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
                    customers.add(toCustomer(record));
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
        customer.setExternalId(record.get("customerID"));
        customer.setGender(record.get("gender"));
        customer.setSeniorCitizen(parseInt(record.get("SeniorCitizen"), "SeniorCitizen"));
        customer.setPartner(record.get("Partner"));
        customer.setDependents(record.get("Dependents"));
        customer.setTenure(parseInt(record.get("tenure"), "tenure"));
        customer.setPhoneService(record.get("PhoneService"));
        customer.setMultipleLines(record.get("MultipleLines"));
        customer.setInternetService(record.get("InternetService"));
        customer.setOnlineSecurity(record.get("OnlineSecurity"));
        customer.setOnlineBackup(record.get("OnlineBackup"));
        customer.setDeviceProtection(record.get("DeviceProtection"));
        customer.setTechSupport(record.get("TechSupport"));
        customer.setStreamingTv(record.get("StreamingTV"));
        customer.setStreamingMovies(record.get("StreamingMovies"));
        customer.setContract(record.get("Contract"));
        customer.setPaperlessBilling(record.get("PaperlessBilling"));
        customer.setPaymentMethod(record.get("PaymentMethod"));
        customer.setMonthlyCharges(parseDouble(record.get("MonthlyCharges"), "MonthlyCharges"));
        customer.setTotalCharges(parseDouble(record.get("TotalCharges"), "TotalCharges"));
        return customer;
    }

    private int parseInt(String value, String column) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("blank " + column);
        }
        return Integer.parseInt(value);
    }

    private double parseDouble(String value, String column) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("blank " + column);
        }
        return Double.parseDouble(value);
    }
}
