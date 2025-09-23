package com.dsevSport.DSEV_Sport.commerce.repository;

import com.dsevSport.DSEV_Sport.commerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByCategoryId(UUID categoryId);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByIsActiveTrue();
}
