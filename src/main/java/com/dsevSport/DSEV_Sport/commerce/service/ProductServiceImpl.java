package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.ProductRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ProductResponse;
import com.dsevSport.DSEV_Sport.commerce.mapper.ProductMapper;
import com.dsevSport.DSEV_Sport.commerce.mapper.ProductVariantMapper;
import com.dsevSport.DSEV_Sport.commerce.model.Product;
import com.dsevSport.DSEV_Sport.commerce.repository.ProductRepository;
import com.dsevSport.DSEV_Sport.commerce.repository.ProductVariantRepository;
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
    private final ProductVariantRepository variantRepository;
    private final ProductVariantMapper variantMapper;

    @Override
    public List<ProductResponse> getAllProducts() {
        List<ProductResponse> list = mapper.toResponseList(repository.findAll());
        list.forEach(p -> p.setVariants(variantMapper.toResponseList(variantRepository.findByProductId(p.getId()))));
        return list;
    }

    @Override
    public ProductResponse getProductById(UUID id) {
        ProductResponse resp = repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        resp.setVariants(variantMapper.toResponseList(variantRepository.findByProductId(id)));
        return resp;
    }

    @Override
    public List<ProductResponse> getProductsByCategory(UUID categoryId) {
        List<ProductResponse> list = mapper.toResponseList(repository.findByCategoryId(categoryId));
        list.forEach(p -> p.setVariants(variantMapper.toResponseList(variantRepository.findByProductId(p.getId()))));
        return list;
    }

    @Override
    public List<ProductResponse> searchProductsByName(String name) {
        List<ProductResponse> list = mapper.toResponseList(repository.findByNameContainingIgnoreCase(name));
        list.forEach(p -> p.setVariants(variantMapper.toResponseList(variantRepository.findByProductId(p.getId()))));
        return list;
    }

    @Override
    public List<ProductResponse> getActiveProducts() {
        List<ProductResponse> list = mapper.toResponseList(repository.findByIsActiveTrue());
        list.forEach(p -> p.setVariants(variantMapper.toResponseList(variantRepository.findByProductId(p.getId()))));
        return list;
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        Product product = mapper.toEntity(request);
        product.setId(UUID.randomUUID());
        ProductResponse resp = mapper.toResponse(repository.save(product));
        resp.setVariants(variantMapper.toResponseList(variantRepository.findByProductId(resp.getId())));
        return resp;
    }

    @Override
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        mapper.updateEntity(request, product);
        ProductResponse resp = mapper.toResponse(repository.save(product));
        resp.setVariants(variantMapper.toResponseList(variantRepository.findByProductId(id)));
        return resp;
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
        Specification<Product> spec = null;

        if (search != null && !search.isBlank()) {
            Specification<Product> s = ProductSpecification.nameOrDescriptionContains(search.trim());
            if (s != null) spec = spec == null ? s : spec.and(s);
        }

        if (brand != null && !brand.isBlank()) {
            Specification<Product> s = ProductSpecification.hasBrand(brand.trim());
            if (s != null) spec = spec == null ? s : spec.and(s);
        }

        if (categoryId != null) {
            Specification<Product> s = ProductSpecification.hasCategory(categoryId);
            if (s != null) spec = spec == null ? s : spec.and(s);
        }

        Specification<Product> priceSpec = ProductSpecification.priceBetween(minPrice, maxPrice);
        if (priceSpec != null) spec = spec == null ? priceSpec : spec.and(priceSpec);

        Specification<Product> activeSpec = ProductSpecification.isActive(active);
        if (activeSpec != null) spec = spec == null ? activeSpec : spec.and(activeSpec);

        return repository.findAll(spec, pageable)
                .map(product -> {
                    ProductResponse resp = mapper.toResponse(product);
                    resp.setVariants(variantMapper.toResponseList(variantRepository.findByProductId(resp.getId())));
                    return resp;
                });
    }
}