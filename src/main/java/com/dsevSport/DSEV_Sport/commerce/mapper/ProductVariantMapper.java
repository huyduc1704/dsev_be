package com.dsevSport.DSEV_Sport.commerce.mapper;

import com.dsevSport.DSEV_Sport.commerce.dto.request.ProductVariantRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ProductVariantResponse;
import com.dsevSport.DSEV_Sport.commerce.model.ProductVariant;
import org.springframework.stereotype.Component;

@Component
public class ProductVariantMapper implements CrudMapper<ProductVariant, ProductVariantResponse, ProductVariantRequest, ProductVariantRequest> {
    @Override
    public ProductVariantResponse toResponse(ProductVariant entity) {
        return ProductVariantResponse.builder()
                .id(entity.getId())
                .productId(entity.getProduct().getId())
                .color(entity.getColor())
                .size(entity.getSize())
                .price(entity.getPrice())
                .stockQuantity(entity.getStockQuantity())
                .build();
    }

    @Override
    public ProductVariant toEntity(ProductVariantRequest request) {
        return ProductVariant.builder()
                .color(request.getColor())
                .size(request.getSize())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .build();
    }

    @Override
    public void updateEntity(ProductVariantRequest request, ProductVariant entity) {
        setIfNotNull(request.getColor(), entity::setColor);
        setIfNotNull(request.getSize(), entity::setSize);
        setIfNotNull(request.getPrice(), entity::setPrice);
        setIfNotNull(request.getStockQuantity(), entity::setStockQuantity);
    }
}
