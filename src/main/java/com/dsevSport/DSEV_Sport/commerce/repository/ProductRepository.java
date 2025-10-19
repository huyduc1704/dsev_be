package com.dsevSport.DSEV_Sport.commerce.repository;

import com.dsevSport.DSEV_Sport.commerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    List<Product> findByCategoryId(UUID categoryId);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByIsActiveTrue();

    // Custom query
    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN ProductVariant pv ON pv.product.id = p.id " +
            "WHERE " +
            "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:brand IS NULL OR LOWER(p.brand) = LOWER(:brand)) AND " +
            "(:minPrice IS NULL OR pv.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR pv.price <= :maxPrice) AND " +
            "(:active IS NULL OR p.isActive = :active) AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId)")
    Page<Product> filterProducts(
            @Param("search") String search,
            @Param("brand") String brand,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("active") Boolean active,
            @Param("categoryId") UUID categoryId,
            Pageable pageable
    );

}
