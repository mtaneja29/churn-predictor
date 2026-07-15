package com.example.churnpoc.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CustomerFeatures {

    @Min(value = 0, message = "Tenure cannot be negative")
    @Max(value = 100, message = "Tenure must be 100 months or less")
    private int tenure;

    @DecimalMin(value = "0.0", message = "Monthly charges cannot be negative")
    @DecimalMax(value = "200.0", message = "Monthly charges must be 200 or less")
    private double monthlyCharges;

    @DecimalMin(value = "0.0", message = "Total charges cannot be negative")
    @DecimalMax(value = "10000.0", message = "Total charges must be 10,000 or less")
    private double totalCharges;

    @NotNull(message = "Contract is required")
    private String contract;

    public CustomerFeatures() {
    }

    public int getTenure() {
        return tenure;
    }

    public void setTenure(int tenure) {
        this.tenure = tenure;
    }

    public double getMonthlyCharges() {
        return monthlyCharges;
    }

    public void setMonthlyCharges(double monthlyCharges) {
        this.monthlyCharges = monthlyCharges;
    }

    public double getTotalCharges() {
        return totalCharges;
    }

    public void setTotalCharges(double totalCharges) {
        this.totalCharges = totalCharges;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }
}
