package com.orderflow.service.impl;

import com.orderflow.exception.BadRequestException;
import com.orderflow.exception.InsufficientStockException;
import com.orderflow.exception.OrderProcessingException;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.model.entity.*;
import com.orderflow.model.enums.OrderStatus;
import com.orderflow.repository.CustomerRepository;
import com.orderflow.repository.OrderRepository;
import com.orderflow.repository.ProductRepository;
import com.orderflow.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Order createOrder(Order order) {
        // Fetch fresh customer from database
        Customer customer = customerRepository.findById(order.getCustomer().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (!customer.getActive()) {
            throw new BadRequestException("Cannot create order for inactive customer");
        }

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new BadRequestException("Order must have at least one item");
        }

        // Set customer
        order.setCustomer(customer);
        
        // Generate order number
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal subtotal = BigDecimal.ZERO;

        // Process order items
        for (OrderItem item : order.getItems()) {
            // Fetch fresh product from database
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.getProduct().getId()));

            // Reserve stock
            product.reserveStock(item.getQuantity());
            productRepository.save(product);

            // Set item properties
            item.setOrder(order);
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setProductSku(product.getSku());
            item.setUnitPrice(product.getPrice());
            
            BigDecimal lineTotal = product.getPrice().multiply(new BigDecimal(item.getQuantity()));
            if (item.getDiscount() != null) {
                lineTotal = lineTotal.subtract(item.getDiscount());
            }
            item.setLineTotal(lineTotal);
            
            subtotal = subtotal.add(lineTotal);
        }

        order.setSubtotal(subtotal);
        
        BigDecimal taxAmount = subtotal.multiply(new BigDecimal("0.18"));
        order.setTaxAmount(taxAmount);
        
        BigDecimal totalAmount = subtotal.add(taxAmount);
        if (order.getDiscountAmount() != null) {
            totalAmount = totalAmount.subtract(order.getDiscountAmount());
        }
        order.setTotalAmount(totalAmount);

        return orderRepository.save(order);
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Override
    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Page<Order> getOrdersByCustomer(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerId(customerId, pageable);
    }

    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    public List<Order> getOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findOrdersBetweenDates(startDate, endDate);
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = getOrderById(orderId);
        
        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new OrderProcessingException(
                "Cannot transition order from " + order.getStatus() + " to " + newStatus
            );
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        if (newStatus == OrderStatus.CONFIRMED && oldStatus == OrderStatus.PENDING) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.reduceStock(item.getQuantity());
                productRepository.save(product);
            }
            updateCustomerAfterOrder(order);
        }

        return orderRepository.save(order);
    }

    @Override
    public void cancelOrder(Long orderId, String reason) {
        Order order = getOrderById(orderId);

        if (!order.getStatus().canTransitionTo(OrderStatus.CANCELLED)) {
            throw new OrderProcessingException("Cannot cancel order in status: " + order.getStatus());
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.releaseReservedStock(item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        if (reason != null) {
            order.setNotes(reason);
        }
        orderRepository.save(order);
    }

    private void updateCustomerAfterOrder(Order order) {
        Customer customer = order.getCustomer();
        
        Integer currentOrders = customer.getTotalOrders() != null ? customer.getTotalOrders() : 0;
        customer.setTotalOrders(currentOrders + 1);
        
        BigDecimal currentSpent = customer.getTotalSpent() != null ? customer.getTotalSpent() : BigDecimal.ZERO;
        customer.setTotalSpent(currentSpent.add(order.getTotalAmount()));
        
        customer.autoUpdateSegment();
        customerRepository.save(customer);
    }
}
