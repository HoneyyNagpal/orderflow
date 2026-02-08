package com.orderflow.repository;

import com.orderflow.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCode(String code);
    List<Category> findByParentIsNull();
    List<Category> findByParent(Category parent);
    boolean existsByCode(String code);
    
    @Query("SELECT c FROM Category c WHERE c.deleted = false AND c.active = true")
    List<Category> findAllActiveCategories();
}
