package com.orderflow.repository;

import com.orderflow.model.entity.Customer;
import com.orderflow.model.entity.Invoice;
import com.orderflow.model.entity.Order;
import com.orderflow.model.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Optional<Invoice> findByOrder(Order order);
    List<Invoice> findByCustomer(Customer customer);
    Page<Invoice> findByCustomer(Customer customer, Pageable pageable);
    List<Invoice> findByStatus(InvoiceStatus status);
    
    @Query("SELECT i FROM Invoice i WHERE i.dueDate < CURRENT_DATE AND i.status = 'SENT'")
    List<Invoice> findOverdueInvoices();
    
    @Query("SELECT i FROM Invoice i WHERE i.status = :status AND i.dueDate < :date")
    List<Invoice> findByStatusAndDueDateBefore(InvoiceStatus status, LocalDate date);
}
