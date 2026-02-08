package com.orderflow.service.impl;

import com.orderflow.exception.BadRequestException;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.model.entity.Customer;
import com.orderflow.model.entity.Invoice;
import com.orderflow.model.entity.Order;
import com.orderflow.model.enums.InvoiceStatus;
import com.orderflow.model.enums.OrderStatus;
import com.orderflow.repository.CustomerRepository;
import com.orderflow.repository.InvoiceRepository;
import com.orderflow.repository.OrderRepository;
import com.orderflow.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public InvoiceServiceImpl(InvoiceRepository invoiceRepository,
                             OrderRepository orderRepository,
                             CustomerRepository customerRepository) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public Invoice createInvoice(Invoice invoice) {
        // Generate invoice number
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isEmpty()) {
            invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        
        // Set invoice date
        if (invoice.getInvoiceDate() == null) {
            invoice.setInvoiceDate(LocalDate.now());
        }
        
        // Set due date (30 days from invoice date)
        if (invoice.getDueDate() == null) {
            invoice.setDueDate(invoice.getInvoiceDate().plusDays(30));
        }
        
        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice generateInvoiceForOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        
        // Check if invoice already exists
        if (invoiceRepository.findByOrder(order).isPresent()) {
            throw new BadRequestException("Invoice already exists for this order");
        }
        
        // Only generate invoice for confirmed or completed orders
        if (order.getStatus() != OrderStatus.CONFIRMED && 
            order.getStatus() != OrderStatus.PROCESSING &&
            order.getStatus() != OrderStatus.SHIPPED &&
            order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Can only generate invoice for confirmed or completed orders");
        }
        
        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setCustomer(order.getCustomer());
        invoice.setSubtotal(order.getSubtotal());
        invoice.setTaxAmount(order.getTaxAmount());
        invoice.setTotalAmount(order.getTotalAmount());
        invoice.setStatus(InvoiceStatus.DRAFT);
        
        return createInvoice(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice", "number", invoiceNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice getInvoiceByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return invoiceRepository.findByOrder(order)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice for order", "orderId", orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> getInvoicesByCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
        return invoiceRepository.findByCustomer(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Invoice> getOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices();
    }

    @Override
    public Invoice updateInvoiceStatus(Long invoiceId, InvoiceStatus status) {
        Invoice invoice = getInvoiceById(invoiceId);
        invoice.setStatus(status);
        return invoiceRepository.save(invoice);
    }

    @Override
    public void markInvoiceAsPaid(Long invoiceId) {
        Invoice invoice = getInvoiceById(invoiceId);
        invoice.markAsPaid();
        invoiceRepository.save(invoice);
    }
}
