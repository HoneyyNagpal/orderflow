package com.orderflow.service;

import com.orderflow.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Product createProduct(Product product);
    Product updateProduct(Long id, Product product);
    Product getProductById(Long id);
    Product getProductBySku(String sku);
    List<Product> getAllProducts();
    Page<Product> getActiveProducts(Pageable pageable);
    List<Product> getLowStockProducts();
    Page<Product> searchProducts(String keyword, Pageable pageable);
    void deleteProduct(Long id);
    void updateStock(Long productId, Integer quantity);
    boolean existsBySku(String sku);
}
