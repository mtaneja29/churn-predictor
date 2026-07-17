package com.example.churnpoc.service;

import com.example.churnpoc.dto.CustomerReview;
import com.example.churnpoc.dto.DashboardView;

public interface DashboardService {

    DashboardView getDashboard(String band, int page);

    CustomerReview getReview(Long customerId);

    void unflag(Long customerId);

    void clearAll();
}
