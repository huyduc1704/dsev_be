package com.dsevSport.DSEV_Sport.commerce.mapper;

import com.dsevSport.DSEV_Sport.commerce.dto.request.ProductRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ProductResponse;
import com.dsevSport.DSEV_Sport.commerce.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper implements CrudMapper<Product, ProductResponse, ProductRequest, ProductRequest> {
    @Override
    public ProductResponse toResponse(Product entity) {
        return ProductResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .brand(entity.getBrand())
                .imageUrl(entity.getImageUrl())
                .isActive(entity.getIsActive())
                .categoryId(entity.getCategory().getId())
                .build();
    }

    @Override
    public Product toEntity(ProductRequest request) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .brand(request.getBrand())
                .imageUrl(request.getImageUrl())
                .isActive(request.getIsActive())
                .build();
    }

    @Override
    public void updateEntity(ProductRequest request, Product entity) {
        setIfNotNull(request.getName(), entity::setName);
        setIfNotNull(request.getDescription(), entity::setDescription);
        setIfNotNull(request.getBrand(), entity::setBrand);
        setIfNotNull(request.getImageUrl(), entity::setImageUrl);
        setIfNotNull(request.getIsActive(), entity::setIsActive);
    }
}
