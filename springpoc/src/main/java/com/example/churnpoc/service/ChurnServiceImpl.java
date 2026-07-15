package com.example.churnpoc.service;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.churnpoc.dto.ChurnAssessment;
import com.example.churnpoc.dto.CustomerFeatures;

@Service
public class ChurnServiceImpl implements ChurnService {

    private RestClient restClient;

    @Autowired
    public ChurnServiceImpl(@Value("${churn.api.base-url}") String theBaseUrl) {
        // timeouts so a hung Python service can't hang our request threads
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        restClient = RestClient.builder()
                .baseUrl(theBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    // matches the Python JSON field names exactly, so Jackson needs no mapping config
    private record PredictionResponse(double churn_probability, String risk_band) {}

    @Override
    public ChurnAssessment assess(CustomerFeatures theCustomerFeatures) {

        // the Java-camelCase -> Python-casing translation lives here and only here
        Map<String, Object> features = Map.of(
                "tenure", theCustomerFeatures.getTenure(),
                "MonthlyCharges", theCustomerFeatures.getMonthlyCharges(),
                "TotalCharges", theCustomerFeatures.getTotalCharges(),
                "Contract", theCustomerFeatures.getContract());

        try {
            PredictionResponse response = restClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(features)
                    .retrieve()
                    .body(PredictionResponse.class);

            return new ChurnAssessment(
                    response.churn_probability(),
                    String.format("%.1f%%", response.churn_probability() * 100),
                    response.risk_band(),
                    recommendationFor(response.risk_band()));
        }
        catch (RestClientException exc) {
            return new ChurnAssessment(null, null, "UNKNOWN",
                    "Prediction service is unavailable right now. Please try again later.");
        }
    }

    private String recommendationFor(String theRiskBand) {
        return switch (theRiskBand) {
            case "HIGH" -> "Contact customer with a retention offer immediately";
            case "MEDIUM" -> "Include customer in the next retention campaign";
            default -> "No action needed";
        };
    }
}
