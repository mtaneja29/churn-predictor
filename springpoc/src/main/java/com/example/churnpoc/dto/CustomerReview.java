package com.example.churnpoc.dto;

import java.util.List;

import com.example.churnpoc.entity.Customer;
import com.example.churnpoc.entity.PredictionResult;

public class CustomerReview {

    private Customer customer;
    private PredictionResult prediction;
    private String churnPercentage;
    private List<String> reasons;

    public CustomerReview(Customer customer, PredictionResult prediction,
                          String churnPercentage, List<String> reasons) {
        this.customer = customer;
        this.prediction = prediction;
        this.churnPercentage = churnPercentage;
        this.reasons = reasons;
    }

    public Customer getCustomer() {
        return customer;
    }

    public PredictionResult getPrediction() {
        return prediction;
    }

    public String getChurnPercentage() {
        return churnPercentage;
    }

    public List<String> getReasons() {
        return reasons;
    }
}
