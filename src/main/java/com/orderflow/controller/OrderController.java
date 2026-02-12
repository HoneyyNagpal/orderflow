package com.orderflow.controller;

import com.orderflow.model.dto.OrderResponseDTO;
import com.orderflow.model.entity.Order;
import com.orderflow.model.enums.OrderStatus;
import com.orderflow.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody Order order) {
        Order createdOrder = orderService.createOrder(order);
        return new ResponseEntity<>(OrderResponseDTO.from(createdOrder), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<OrderResponseDTO> orders = orderService.getAllOrders()
            .stream()
            .map(OrderResponseDTO::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(OrderResponseDTO.from(orderService.getOrderById(id)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(OrderResponseDTO.from(updatedOrder));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        orderService.cancelOrder(id, reason);
        return ResponseEntity.ok().build();
    }
}
