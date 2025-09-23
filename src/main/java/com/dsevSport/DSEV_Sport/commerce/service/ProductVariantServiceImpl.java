package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.ProductVariantRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ProductVariantResponse;
import com.dsevSport.DSEV_Sport.commerce.mapper.ProductVariantMapper;
import com.dsevSport.DSEV_Sport.commerce.model.ProductVariant;
import com.dsevSport.DSEV_Sport.commerce.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {
    private final ProductVariantRepository repository;
    private final ProductVariantMapper mapper;

    @Override
    public List<ProductVariantResponse> getAllVariants() {
        return mapper.toResponseList(repository.findAll());
    }

    @Override
    public ProductVariantResponse getVariantById(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found with id: " + id));
    }

    @Override
    public List<ProductVariantResponse> getVariantsByProductId(UUID productId) {
        return mapper.toResponseList(repository.findByProductId(productId));
    }

    @Override
    public ProductVariantResponse createVariant(ProductVariantRequest request) {
        ProductVariant variant = mapper.toEntity(request);
        variant.setId(UUID.randomUUID());
        return mapper.toResponse(repository.save(variant));
    }

    @Override
    public ProductVariantResponse updateVariant(UUID id, ProductVariantRequest request) {
        ProductVariant variant = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found with id: " + id));
        mapper.updateEntity(request, variant);
        return mapper.toResponse(repository.save(variant));
    }

    @Override
    public void deleteVariant(UUID id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Variant not found with id: " + id);
        }
        repository.deleteById(id);
    }
}