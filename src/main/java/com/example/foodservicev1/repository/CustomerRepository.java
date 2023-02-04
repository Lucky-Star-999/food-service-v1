package com.example.foodservicev1.repository;

import com.example.foodservicev1.entity.Customer;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository {
    public List<Customer> findAll();
    public int save(Customer customer);
}