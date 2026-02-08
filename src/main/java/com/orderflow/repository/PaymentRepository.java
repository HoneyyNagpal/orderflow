package com.orderflow.repository;

import com.orderflow.model.entity.Invoice;
import com.orderflow.model.entity.Payment;
import com.orderflow.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReferenceNumber(String referenceNumber);
    List<Payment> findByInvoice(Invoice invoice);
    List<Payment> findByStatus(PaymentStatus status);
}
