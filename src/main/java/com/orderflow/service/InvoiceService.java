package com.orderflow.service;

import com.orderflow.model.entity.Invoice;
import com.orderflow.model.enums.InvoiceStatus;

import java.util.List;

public interface InvoiceService {
    Invoice createInvoice(Invoice invoice);
    Invoice generateInvoiceForOrder(Long orderId);
    Invoice getInvoiceById(Long id);
    Invoice getInvoiceByNumber(String invoiceNumber);
    Invoice getInvoiceByOrderId(Long orderId);
    List<Invoice> getInvoicesByCustomer(Long customerId);
    List<Invoice> getOverdueInvoices();
    Invoice updateInvoiceStatus(Long invoiceId, InvoiceStatus status);
    void markInvoiceAsPaid(Long invoiceId);
}
