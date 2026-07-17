package com.example.churnpoc.dao;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.churnpoc.entity.PredictionResult;

public interface PredictionResultRepository extends JpaRepository<PredictionResult, Long> {

    // Spring Data derives the SQL from the method name - no query written
    Page<PredictionResult> findByRiskBand(String riskBand, Pageable pageable);

    long countByRiskBand(String riskBand);

    long countByRiskBandAndActionStatus(String riskBand, String actionStatus);

    Optional<PredictionResult> findByCustomerId(Long customerId);
}
