package com.orderflow.repository;

import com.orderflow.model.entity.Order;
import com.orderflow.model.entity.OrderItem;
import com.orderflow.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
    List<OrderItem> findByProduct(Product product);
}
