package com.example.churnpoc.service;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.example.churnpoc.dao.CustomerRepository;
import com.example.churnpoc.dao.PredictionResultRepository;
import com.example.churnpoc.entity.Customer;
import com.example.churnpoc.entity.PredictionResult;

@Service
public class ScanServiceImpl implements ScanService {

    private CustomerRepository customerRepository;
    private PredictionResultRepository predictionResultRepository;
    private RestClient restClient;

    @Autowired
    public ScanServiceImpl(CustomerRepository theCustomerRepository,
                           PredictionResultRepository thePredictionResultRepository,
                           @Value("${churn.api.base-url}") String theBaseUrl) {
        customerRepository = theCustomerRepository;
        predictionResultRepository = thePredictionResultRepository;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        // a batch of thousands of rows needs far more headroom than a single prediction
        requestFactory.setReadTimeout(Duration.ofSeconds(30));

        restClient = RestClient.builder()
                .baseUrl(theBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    // matches the ML service's JSON exactly
    private record Prediction(double churn_probability, String risk_band) {}

    @Override
    @Transactional  // delete-old + insert-new results is all-or-nothing
    public int scanAll() {
        List<Customer> customers = customerRepository.findAll();
        if (customers.isEmpty()) {
            return 0;
        }

        List<Map<String, Object>> payload = customers.stream()
                .map(this::toFeatures)
                .toList();

        List<Prediction> predictions = restClient.post()
                .uri("/predict-batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Prediction>>() {});

        if (predictions == null || predictions.size() != customers.size()) {
            throw new IllegalStateException("ML service returned "
                    + (predictions == null ? 0 : predictions.size())
                    + " predictions for " + customers.size() + " customers");
        }

        // same order in = same order out, so index i pairs customer with prediction
        LocalDateTime now = LocalDateTime.now();
        List<PredictionResult> results = new ArrayList<>();
        for (int i = 0; i < customers.size(); i++) {
            PredictionResult result = new PredictionResult();
            result.setCustomerId(customers.get(i).getId());
            result.setChurnProbability(predictions.get(i).churn_probability());
            result.setRiskBand(predictions.get(i).risk_band());
            result.setAssessedAt(now);
            result.setActionStatus("PENDING");
            results.add(result);
        }

        predictionResultRepository.deleteAllInBatch();
        predictionResultRepository.saveAll(results);
        return results.size();
    }

    // entity (Java camelCase) -> ML service field names; lives here and only here
    private Map<String, Object> toFeatures(Customer c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("tenure", c.getTenure());
        m.put("MonthlyCharges", c.getMonthlyCharges());
        m.put("TotalCharges", c.getTotalCharges());
        m.put("SeniorCitizen", c.getSeniorCitizen());
        m.put("gender", c.getGender());
        m.put("Partner", c.getPartner());
        m.put("Dependents", c.getDependents());
        m.put("PhoneService", c.getPhoneService());
        m.put("MultipleLines", c.getMultipleLines());
        m.put("InternetService", c.getInternetService());
        m.put("OnlineSecurity", c.getOnlineSecurity());
        m.put("OnlineBackup", c.getOnlineBackup());
        m.put("DeviceProtection", c.getDeviceProtection());
        m.put("TechSupport", c.getTechSupport());
        m.put("StreamingTV", c.getStreamingTv());
        m.put("StreamingMovies", c.getStreamingMovies());
        m.put("Contract", c.getContract());
        m.put("PaperlessBilling", c.getPaperlessBilling());
        m.put("PaymentMethod", c.getPaymentMethod());
        return m;
    }
}
