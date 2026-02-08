package com.orderflow.service.impl;

import com.orderflow.exception.BadRequestException;
import com.orderflow.exception.InsufficientStockException;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.model.entity.Product;
import com.orderflow.repository.ProductRepository;
import com.orderflow.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product createProduct(Product product) {
        if (productRepository.existsBySku(product.getSku())) {
            throw new BadRequestException("Product with SKU " + product.getSku() + " already exists");
        }
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        Product existingProduct = getProductById(id);
        
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setCostPrice(product.getCostPrice());
        existingProduct.setMinStockLevel(product.getMinStockLevel());
        existingProduct.setActive(product.getActive());
        existingProduct.setCategory(product.getCategory());
        
        return productRepository.save(existingProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> getActiveProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchProducts(keyword, pageable);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        product.softDelete();
        productRepository.save(product);
    }

    @Override
    public void updateStock(Long productId, Integer quantity) {
        Product product = getProductById(productId);
        
        if (quantity < 0 && product.getAvailableStock() < Math.abs(quantity)) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }
        
        int newStock = product.getQuantityInStock() + quantity;
        if (newStock < 0) {
            throw new InsufficientStockException("Stock cannot be negative");
        }
        
        product.setQuantityInStock(newStock);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySku(String sku) {
        return productRepository.existsBySku(sku);
    }
}
