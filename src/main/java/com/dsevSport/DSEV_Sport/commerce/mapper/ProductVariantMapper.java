package com.dsevSport.DSEV_Sport.commerce.mapper;

import com.dsevSport.DSEV_Sport.commerce.dto.request.ProductVariantRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ProductVariantResponse;
import com.dsevSport.DSEV_Sport.commerce.model.Product;
import com.dsevSport.DSEV_Sport.commerce.model.ProductVariant;
import org.springframework.stereotype.Component;

@Component
public class ProductVariantMapper implements CrudMapper<ProductVariant, ProductVariantResponse, ProductVariantRequest, ProductVariantRequest> {
    @Override
    public ProductVariantResponse toResponse(ProductVariant entity) {
        return ProductVariantResponse.builder()
                .id(entity.getId())
                .productId(entity.getProduct() != null ? entity.getProduct().getId() : null)
                .color(entity.getColor())
                .size(entity.getSize())
                .price(entity.getPrice())
                .stockQuantity(entity.getStockQuantity())
                .build();
    }

    @Override
    public ProductVariant toEntity(ProductVariantRequest request) {
        ProductVariant variant = ProductVariant.builder()
                .color(request.getColor())
                .size(request.getSize())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .build();
        if (request.getProductId() != null) {
            Product product = new Product();
            product.setId(request.getProductId());
            variant.setProduct(product);
        }
        return variant;
    }

    @Override
    public void updateEntity(ProductVariantRequest request, ProductVariant entity) {
        setIfNotNull(request.getColor(), entity::setColor);
        setIfNotNull(request.getSize(), entity::setSize);
        setIfNotNull(request.getPrice(), entity::setPrice);
        setIfNotNull(request.getStockQuantity(), entity::setStockQuantity);
        if (request.getProductId() != null) {
            Product product = new Product();
            product.setId(request.getProductId());
            entity.setProduct(product);
        }
    }
}
