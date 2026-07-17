package com.example.churnpoc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.churnpoc.dao.CustomerRepository;

@Controller
public class HomeController {

    private CustomerRepository customerRepository;

    @Autowired
    public HomeController(CustomerRepository theCustomerRepository) {
        customerRepository = theCustomerRepository;
    }

    // no data yet -> start at upload; otherwise the dashboard is home
    @GetMapping("/")
    public String home() {
        if (customerRepository.count() == 0) {
            return "redirect:/upload";
        }
        return "redirect:/dashboard";
    }
}
