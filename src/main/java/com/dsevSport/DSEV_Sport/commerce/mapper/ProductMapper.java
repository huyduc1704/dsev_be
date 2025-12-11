package com.dsevSport.DSEV_Sport.commerce.mapper;

import com.dsevSport.DSEV_Sport.commerce.dto.request.ProductRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ProductResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.TagResponse;
import com.dsevSport.DSEV_Sport.commerce.model.Category;
import com.dsevSport.DSEV_Sport.commerce.model.Product;
import com.dsevSport.DSEV_Sport.commerce.model.ProductImage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductMapper implements CrudMapper<Product, ProductResponse, ProductRequest, ProductRequest> {
    @Override
    public ProductResponse toResponse(Product entity) {
        return ProductResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .brand(entity.getBrand())
                .isActive(entity.getIsActive())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .images(
                        entity.getImages() != null
                        ? entity.getImages().stream()
                                .map(ProductImage::getImageUrl)
                                .toList()
                                : List.of()
                )
                .tags(
                        entity.getTags() != null
                                ? entity.getTags().stream()
                                .map(tag -> TagResponse.builder()
                                        .id(tag.getId())
                                        .name(tag.getName())
                                        .displayName(tag.getDisplayName())
                                        .build())
                                .toList()
                                : List.of()
                )
                .build();
    }

    @Override
    public Product toEntity(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .brand(request.getBrand())
                .isActive(request.getIsActive())
                .build();
        if (request.getCategoryId() != null) {
            Category category = new Category();
            category.setId(request.getCategoryId());
            product.setCategory(category);
        }
        return product;
    }

    @Override
    public void updateEntity(ProductRequest request, Product entity) {
        setIfNotNull(request.getName(), entity::setName);
        setIfNotNull(request.getDescription(), entity::setDescription);
        setIfNotNull(request.getBrand(), entity::setBrand);
        setIfNotNull(request.getIsActive(), entity::setIsActive);
        if (request.getCategoryId() != null) {
            Category category = new Category();
            category.setId(request.getCategoryId());
            entity.setCategory(category);
        }
    }
}
