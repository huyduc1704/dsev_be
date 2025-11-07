package com.dsevSport.DSEV_Sport.commerce.specification;

import com.dsevSport.DSEV_Sport.commerce.model.Product;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Expression;

import java.util.UUID;

public class ProductSpecification {
    public static Specification<Product> hasBrand(String brand) {
        return (root, query, cb) -> brand == null || brand.isBlank() ? null : cb.equal(root.get("brand"), brand);
    }

    public static Specification<Product> priceBetween(Double min, Double max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            // adjust the numeric type to match your entity (Double, BigDecimal, etc.)
            Expression<Double> pricePath = root.get("price").as(Double.class);
            if (min == null) return cb.lessThanOrEqualTo(pricePath, max);
            if (max == null) return cb.greaterThanOrEqualTo(pricePath, min);
            return cb.between(pricePath, min, max);
        };
    }

    public static Specification<Product> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("isActive"), active);
    }

    public static Specification<Product> nameOrDescriptionContains(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Product> hasCategory(UUID categoryId) {
        return (root, query, cb) -> categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
    }
}