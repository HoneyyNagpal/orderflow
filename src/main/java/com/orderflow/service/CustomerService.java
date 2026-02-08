package com.orderflow.service;

import com.orderflow.model.entity.Customer;
import com.orderflow.model.enums.CustomerSegment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {
    Customer createCustomer(Customer customer);
    Customer updateCustomer(Long id, Customer customer);
    Customer getCustomerById(Long id);
    Customer getCustomerByEmail(String email);
    Customer getCustomerByCode(String customerCode);
    List<Customer> getAllCustomers();
    Page<Customer> getActiveCustomers(Pageable pageable);
    List<Customer> getCustomersBySegment(CustomerSegment segment);
    void deleteCustomer(Long id);
    boolean existsByEmail(String email);
}
