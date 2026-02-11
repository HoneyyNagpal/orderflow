package com.orderflow.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.orderflow.exception.InsufficientStockException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_sku", columnList = "sku"),
    @Index(name = "idx_product_name", columnList = "name")
})
public class Product extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(nullable = false)
    private Integer quantityInStock = 0;

    @Column(nullable = false)
    private Integer reservedQuantity = 0;

    private Integer minStockLevel;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @JsonIgnore  // Prevent infinite recursion
    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems = new ArrayList<>();

    public void reserveStock(int quantity) {
        if (quantityInStock < quantity) {
            throw new InsufficientStockException("Insufficient stock for product: " + name);
        }
        this.reservedQuantity += quantity;
    }

    public void releaseReservedStock(int quantity) {
        this.reservedQuantity = Math.max(0, this.reservedQuantity - quantity);
    }

    public void reduceStock(int quantity) {
        if (quantityInStock < quantity) {
            throw new InsufficientStockException("Insufficient stock for product: " + name);
        }
        this.quantityInStock -= quantity;
        this.reservedQuantity = Math.max(0, this.reservedQuantity - quantity);
    }

    public void increaseStock(int quantity) {
        this.quantityInStock += quantity;
    }

    @Transient
    public Integer getAvailableStock() {
        return quantityInStock - reservedQuantity;
    }

    @Transient
    public boolean isLowStock() {
        return minStockLevel != null && quantityInStock <= minStockLevel;
    }

    // Getters and Setters
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public Integer getQuantityInStock() { return quantityInStock; }
    public void setQuantityInStock(Integer quantityInStock) { this.quantityInStock = quantityInStock; }

    public Integer getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }

    public Integer getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(Integer minStockLevel) { this.minStockLevel = minStockLevel; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
}
