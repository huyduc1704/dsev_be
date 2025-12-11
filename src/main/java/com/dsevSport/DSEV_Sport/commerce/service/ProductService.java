package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.ProductRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    List<ProductResponse> getAllProducts();
    ProductResponse getProductById(UUID id);
    List<ProductResponse> getProductsByCategory(UUID categoryId);
    List<ProductResponse> searchProductsByName(String name);
    List<ProductResponse> getActiveProducts();
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(UUID id, ProductRequest request);
    void deleteProduct(UUID id);
    Page<ProductResponse> filterProducts(
            String search,
            String brand,
            Double minPrice,
            Double maxPrice,
            Boolean active,
            UUID categoryId,
            Pageable pageable);
    ProductResponse addTagsToProduct(UUID productId, List<UUID> tagIds);
    ProductResponse removeTagFromProduct(UUID productId, UUID tagId);
}
