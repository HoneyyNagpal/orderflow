package com.orderflow.service.impl;

import com.orderflow.exception.BadRequestException;
import com.orderflow.exception.OrderProcessingException;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.model.entity.Customer;
import com.orderflow.model.entity.Order;
import com.orderflow.model.entity.OrderItem;
import com.orderflow.model.entity.Product;
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

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                           CustomerRepository customerRepository,
                           ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Order createOrder(Order order) {
        // Validate customer
        Customer customer = customerRepository.findById(order.getCustomer().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", order.getCustomer().getId()));
        
        if (!customer.getActive()) {
            throw new BadRequestException("Cannot create order for inactive customer");
        }
        
        // Generate order number
        if (order.getOrderNumber() == null || order.getOrderNumber().isEmpty()) {
            order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        
        // Set order date
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
        
        // Process order items
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }
        
        for (OrderItem item : order.getItems()) {
            // Get product
            Product product = productRepository.findById(item.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", item.getProduct().getId()));
            
            // Check stock availability
            if (product.getAvailableStock() < item.getQuantity()) {
                throw new OrderProcessingException(
                    "Insufficient stock for product: " + product.getName() + 
                    ". Available: " + product.getAvailableStock()
                );
            }
            
            // Reserve stock
            product.reserveStock(item.getQuantity());
            productRepository.save(product);
            
            // Set item details from product
            item.setProductName(product.getName());
            item.setProductSku(product.getSku());
            item.setUnitPrice(product.getPrice());
            
            // Calculate line total
            item.calculateLineTotal();
            item.setOrder(order);
        }
        
        // Calculate order totals
        order.calculateTotal();
        
        // Set initial status
        order.setStatus(OrderStatus.PENDING);
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        // Update customer statistics
        customer.updateAfterOrder(order.getTotalAmount());
        customerRepository.save(customer);
        
        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "number", orderNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByCustomer(Long customerId, Pageable pageable) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
        return orderRepository.findByCustomer(customer, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = getOrderById(orderId);
        
        try {
            order.updateStatus(newStatus);
        } catch (IllegalStateException e) {
            throw new OrderProcessingException(e.getMessage());
        }
        
        // Handle stock when order is confirmed
        if (newStatus == OrderStatus.CONFIRMED) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.reduceStock(item.getQuantity());
                productRepository.save(product);
            }
        }
        
        return orderRepository.save(order);
    }

    @Override
    public void cancelOrder(Long orderId, String reason) {
        Order order = getOrderById(orderId);
        
        // Can only cancel if not yet shipped/delivered
        if (order.getStatus() == OrderStatus.SHIPPED || 
            order.getStatus() == OrderStatus.DELIVERED) {
            throw new OrderProcessingException("Cannot cancel order that is already shipped or delivered");
        }
        
        // Release reserved stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.releaseReservedStock(item.getQuantity());
            productRepository.save(product);
        }
        
        order.updateStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findOrdersBetweenDates(startDate, endDate);
    }
}
