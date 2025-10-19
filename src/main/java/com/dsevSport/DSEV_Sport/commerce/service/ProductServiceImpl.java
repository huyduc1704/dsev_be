package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.ProductRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ProductResponse;
import com.dsevSport.DSEV_Sport.commerce.mapper.ProductMapper;
import com.dsevSport.DSEV_Sport.commerce.model.Product;
import com.dsevSport.DSEV_Sport.commerce.repository.ProductRepository;
import com.dsevSport.DSEV_Sport.commerce.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Override
    public List<ProductResponse> getAllProducts() {
        return mapper.toResponseList(repository.findAll());
    }

    @Override
    public ProductResponse getProductById(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
    }

    @Override
    public List<ProductResponse> getProductsByCategory(UUID categoryId) {
        return mapper.toResponseList(repository.findByCategoryId(categoryId));
    }

    @Override
    public List<ProductResponse> searchProductsByName(String name) {
        return mapper.toResponseList(repository.findByNameContainingIgnoreCase(name));
    }

    @Override
    public List<ProductResponse> getActiveProducts() {
        return mapper.toResponseList(repository.findByIsActiveTrue());
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        Product product = mapper.toEntity(request);
        product.setId(UUID.randomUUID());
        return mapper.toResponse(repository.save(product));
    }

    @Override
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        mapper.updateEntity(request, product);
        return mapper.toResponse(repository.save(product));
    }

    @Override
    public void deleteProduct(UUID id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with id: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public Page<ProductResponse> filterProducts(String search, String brand, Double minPrice, Double maxPrice, Boolean active, UUID categoryId, Pageable pageable) {
        Specification<Product> spec = ProductSpecification.hasBrand(brand)
                .and(ProductSpecification.priceBetween(minPrice, maxPrice))
                .and(ProductSpecification.isActive(active));
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }
}
