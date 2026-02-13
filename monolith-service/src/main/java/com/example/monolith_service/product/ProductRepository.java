package com.example.monolith_service.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(
        String name,
        String sku,
        Pageable pageable
    );
}
