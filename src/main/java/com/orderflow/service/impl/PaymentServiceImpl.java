package com.orderflow.service.impl;

import com.orderflow.exception.BadRequestException;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.model.entity.Invoice;
import com.orderflow.model.entity.Payment;
import com.orderflow.model.enums.InvoiceStatus;
import com.orderflow.model.enums.PaymentStatus;
import com.orderflow.repository.InvoiceRepository;
import com.orderflow.repository.PaymentRepository;
import com.orderflow.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository,
                             InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public Payment createPayment(Payment payment) {
        // Generate reference number
        if (payment.getReferenceNumber() == null || payment.getReferenceNumber().isEmpty()) {
            payment.setReferenceNumber("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        
        return paymentRepository.save(payment);
    }

    @Override
    public Payment processPayment(Long invoiceId, Payment payment) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));
        
        // Validate payment amount
        BigDecimal balance = invoice.getBalanceAmount();
        if (payment.getAmount().compareTo(balance) > 0) {
            throw new BadRequestException("Payment amount exceeds invoice balance");
        }
        
        if (payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Payment amount must be greater than zero");
        }
        
        // Set invoice reference
        payment.setInvoice(invoice);
        payment.setStatus(PaymentStatus.PROCESSING);
        
        // Create payment
        Payment savedPayment = createPayment(payment);
        
        // Simulate payment processing (in real app, this would call payment gateway)
        // For now, automatically mark as completed
        savedPayment.markAsCompleted("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        savedPayment = paymentRepository.save(savedPayment);
        
        // Update invoice
        BigDecimal newPaidAmount = invoice.getPaidAmount().add(payment.getAmount());
        invoice.setPaidAmount(newPaidAmount);
        
        if (newPaidAmount.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.markAsPaid();
        }
        
        invoiceRepository.save(invoice);
        
        return savedPayment;
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentByReference(String referenceNumber) {
        return paymentRepository.findByReferenceNumber(referenceNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", "reference", referenceNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));
        return paymentRepository.findByInvoice(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    @Override
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Payment payment = getPaymentById(paymentId);
        payment.setStatus(status);
        return paymentRepository.save(payment);
    }
}
