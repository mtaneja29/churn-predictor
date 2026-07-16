package com.example.churnpoc.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.churnpoc.entity.PredictionResult;

public interface PredictionResultRepository extends JpaRepository<PredictionResult, Long> {
}
