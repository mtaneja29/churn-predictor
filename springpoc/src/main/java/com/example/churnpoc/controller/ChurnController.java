package com.example.churnpoc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.churnpoc.dto.CustomerFeatures;
import com.example.churnpoc.service.ChurnService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/churn")
public class ChurnController {

    private ChurnService churnService;

    @Autowired
    public ChurnController(ChurnService theChurnService) {
        churnService = theChurnService;
    }

    @GetMapping("/form")
    public String showForm(Model theModel) {
        theModel.addAttribute("customerFeatures", new CustomerFeatures());
        return "churn-form";
    }

    @PostMapping("/assess")
    public String assess(
            @Valid @ModelAttribute("customerFeatures") CustomerFeatures theCustomerFeatures,
            BindingResult theBindingResult,
            Model theModel) {

        if (theBindingResult.hasErrors()) {
            return "churn-form";
        }

        theModel.addAttribute("assessment", churnService.assess(theCustomerFeatures));
        return "churn-result";
    }
}
