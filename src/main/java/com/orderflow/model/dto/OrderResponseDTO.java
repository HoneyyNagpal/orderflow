package com.orderflow.model.dto;

import com.orderflow.model.entity.Order;
import com.orderflow.model.entity.OrderItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderResponseDTO {
    private Long id;
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    private Long customerId;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String notes;
    private List<OrderItemDTO> items;

    public static OrderResponseDTO from(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus().name());
        dto.setSubtotal(order.getSubtotal());
        dto.setTaxAmount(order.getTaxAmount());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setNotes(order.getNotes());

        if (order.getCustomer() != null) {
            dto.setCustomerId(order.getCustomer().getId());
            dto.setCustomerName(order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName());
            dto.setCustomerEmail(order.getCustomer().getEmail());
        }

        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                .map(OrderItemDTO::from)
                .collect(Collectors.toList()));
        }

        return dto;
    }

    public static class OrderItemDTO {
        private Long id;
        private String productName;
        private String productSku;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;

        public static OrderItemDTO from(OrderItem item) {
            OrderItemDTO dto = new OrderItemDTO();
            dto.setId(item.getId());
            dto.setProductName(item.getProductName());
            dto.setProductSku(item.getProductSku());
            dto.setQuantity(item.getQuantity());
            dto.setUnitPrice(item.getUnitPrice());
            dto.setLineTotal(item.getLineTotal());
            return dto;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getProductSku() { return productSku; }
        public void setProductSku(String productSku) { this.productSku = productSku; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getLineTotal() { return lineTotal; }
        public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
}
