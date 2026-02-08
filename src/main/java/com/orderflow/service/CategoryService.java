package com.orderflow.service;

import com.orderflow.model.entity.Category;

import java.util.List;

public interface CategoryService {
    Category createCategory(Category category);
    Category updateCategory(Long id, Category category);
    Category getCategoryById(Long id);
    Category getCategoryByCode(String code);
    List<Category> getAllCategories();
    List<Category> getRootCategories();
    List<Category> getSubCategories(Long parentId);
    void deleteCategory(Long id);
}
