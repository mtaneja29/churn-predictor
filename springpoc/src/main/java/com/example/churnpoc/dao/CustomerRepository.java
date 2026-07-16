package com.example.churnpoc.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.churnpoc.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
