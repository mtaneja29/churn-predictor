package com.example.churnpoc.service;

public interface ScanService {

    /**
     * Scores every customer in the database via the ML service
     * and replaces the prediction_result table with fresh results.
     *
     * @return number of customers scored
     */
    int scanAll();
}
