package com.example.churnpoc.dto;

import java.util.List;

public class DashboardView {

    private long totalCustomers;
    private long totalPendingCount;
    private long highCount;
    private long highPendingCount;
    private long mediumCount;
    private long mediumPendingCount;
    private long lowCount;
    private long lowPendingCount;
    private boolean scored;
    private List<RiskRow> rows;
    private String band;
    private int page;
    private int totalPages;

    public DashboardView(long totalCustomers, long totalPendingCount, long highCount, long highPendingCount,
                         long mediumCount, long mediumPendingCount, long lowCount, long lowPendingCount, boolean scored, List<RiskRow> rows,
                         String band, int page, int totalPages) {
        this.totalCustomers = totalCustomers;
        this.totalPendingCount = totalPendingCount;
        this.highCount = highCount;
        this.highPendingCount = highPendingCount;
        this.mediumCount = mediumCount;
        this.mediumPendingCount = mediumPendingCount;
        this.lowCount = lowCount;
        this.lowPendingCount = lowPendingCount;
        this.scored = scored;
        this.rows = rows;
        this.band = band;
        this.page = page;
        this.totalPages = totalPages;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public long getTotalPendingCount() {
        return totalPendingCount;
    }

    public long getHighCount() {
        return highCount;
    }

    public long getHighPendingCount() {
        return highPendingCount;
    }

    public long getMediumCount() {
        return mediumCount;
    }

    public long getMediumPendingCount() {
        return mediumPendingCount;
    }

    public long getLowCount() {
        return lowCount;
    }

    public long getLowPendingCount() {
        return lowPendingCount;
    }

    public boolean isScored() {
        return scored;
    }

    public List<RiskRow> getRows() {
        return rows;
    }

    public String getBand() {
        return band;
    }

    public int getPage() {
        return page;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
