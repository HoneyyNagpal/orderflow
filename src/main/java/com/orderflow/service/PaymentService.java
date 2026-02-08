package com.orderflow.service;

import com.orderflow.model.entity.Payment;
import com.orderflow.model.enums.PaymentStatus;

import java.util.List;

public interface PaymentService {
    Payment createPayment(Payment payment);
    Payment processPayment(Long invoiceId, Payment payment);
    Payment getPaymentById(Long id);
    Payment getPaymentByReference(String referenceNumber);
    List<Payment> getPaymentsByInvoice(Long invoiceId);
    List<Payment> getPaymentsByStatus(PaymentStatus status);
    Payment updatePaymentStatus(Long paymentId, PaymentStatus status);
}
