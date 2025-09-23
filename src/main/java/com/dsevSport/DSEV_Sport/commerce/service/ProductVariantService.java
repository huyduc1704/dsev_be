package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.ProductVariantRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ProductVariantResponse;

import java.util.List;
import java.util.UUID;

public interface ProductVariantService {
    List<ProductVariantResponse> getAllVariants();
    ProductVariantResponse getVariantById(UUID id);
    List<ProductVariantResponse> getVariantsByProductId(UUID productId);
    ProductVariantResponse createVariant(ProductVariantRequest request);
    ProductVariantResponse updateVariant(UUID id, ProductVariantRequest request);
    void deleteVariant(UUID id);
}
