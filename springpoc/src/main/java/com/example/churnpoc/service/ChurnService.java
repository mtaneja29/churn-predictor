package com.example.churnpoc.service;

import com.example.churnpoc.dto.ChurnAssessment;
import com.example.churnpoc.dto.CustomerFeatures;

public interface ChurnService {

    ChurnAssessment assess(CustomerFeatures theCustomerFeatures);
}
