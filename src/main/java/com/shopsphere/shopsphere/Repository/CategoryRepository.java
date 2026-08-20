package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}