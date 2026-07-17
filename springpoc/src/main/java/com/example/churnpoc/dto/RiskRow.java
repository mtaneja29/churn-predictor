package com.example.churnpoc.dto;

public class RiskRow {

    private Long customerId;
    private String externalId;
    private String contract;
    private Integer tenure;
    private Double monthlyCharges;
    private String churnPercentage;
    private String riskBand;
    private String actionStatus;

    public RiskRow(Long customerId, String externalId, String contract, Integer tenure,
                   Double monthlyCharges, String churnPercentage, String riskBand,
                   String actionStatus) {
        this.customerId = customerId;
        this.externalId = externalId;
        this.contract = contract;
        this.tenure = tenure;
        this.monthlyCharges = monthlyCharges;
        this.churnPercentage = churnPercentage;
        this.riskBand = riskBand;
        this.actionStatus = actionStatus;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getContract() {
        return contract;
    }

    public Integer getTenure() {
        return tenure;
    }

    public Double getMonthlyCharges() {
        return monthlyCharges;
    }

    public String getChurnPercentage() {
        return churnPercentage;
    }

    public String getRiskBand() {
        return riskBand;
    }

    public String getActionStatus() {
        return actionStatus;
    }
}
