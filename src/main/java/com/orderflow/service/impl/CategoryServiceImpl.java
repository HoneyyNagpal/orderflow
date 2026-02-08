package com.orderflow.service.impl;

import com.orderflow.exception.BadRequestException;
import com.orderflow.exception.ResourceNotFoundException;
import com.orderflow.model.entity.Category;
import com.orderflow.repository.CategoryRepository;
import com.orderflow.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category createCategory(Category category) {
        if (categoryRepository.existsByCode(category.getCode())) {
            throw new BadRequestException("Category with code " + category.getCode() + " already exists");
        }
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Long id, Category category) {
        Category existingCategory = getCategoryById(id);
        
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        existingCategory.setActive(category.getActive());
        existingCategory.setParent(category.getParent());
        
        return categoryRepository.save(existingCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryByCode(String code) {
        return categoryRepository.findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Category", "code", code));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getRootCategories() {
        return categoryRepository.findByParentIsNull();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getSubCategories(Long parentId) {
        Category parent = getCategoryById(parentId);
        return categoryRepository.findByParent(parent);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        category.softDelete();
        categoryRepository.save(category);
    }
}
