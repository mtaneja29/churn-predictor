package com.example.churnpoc.dto;

public class ChurnAssessment {

    private Double churnProbability;
    private String churnPercentage;
    private String riskBand;
    private String recommendation;

    public ChurnAssessment() {
    }

    public ChurnAssessment(Double churnProbability, String churnPercentage,
                           String riskBand, String recommendation) {
        this.churnProbability = churnProbability;
        this.churnPercentage = churnPercentage;
        this.riskBand = riskBand;
        this.recommendation = recommendation;
    }

    public Double getChurnProbability() {
        return churnProbability;
    }

    public void setChurnProbability(Double churnProbability) {
        this.churnProbability = churnProbability;
    }

    public String getChurnPercentage() {
        return churnPercentage;
    }

    public void setChurnPercentage(String churnPercentage) {
        this.churnPercentage = churnPercentage;
    }

    public String getRiskBand() {
        return riskBand;
    }

    public void setRiskBand(String riskBand) {
        this.riskBand = riskBand;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}
