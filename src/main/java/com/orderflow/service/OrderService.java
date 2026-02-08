package com.orderflow.service;

import com.orderflow.model.entity.Order;
import com.orderflow.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    Order createOrder(Order order);
    Order getOrderById(Long id);
    Order getOrderByNumber(String orderNumber);
    List<Order> getAllOrders();
    Page<Order> getOrdersByCustomer(Long customerId, Pageable pageable);
    List<Order> getOrdersByStatus(OrderStatus status);
    Order updateOrderStatus(Long orderId, OrderStatus newStatus);
    void cancelOrder(Long orderId, String reason);
    List<Order> getOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate);
}
