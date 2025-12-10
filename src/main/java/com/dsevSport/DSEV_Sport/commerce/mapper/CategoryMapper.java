package com.dsevSport.DSEV_Sport.commerce.mapper;

import com.dsevSport.DSEV_Sport.commerce.dto.request.CategoryRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.CategoryResponse;
import com.dsevSport.DSEV_Sport.commerce.model.Category;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CategoryMapper implements CrudMapper<Category, CategoryResponse, CategoryRequest, CategoryRequest> {

    @Override
    public CategoryResponse toResponse(Category entity) {
        return CategoryResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .build();
    }

    @Override
    public Category toEntity(CategoryRequest categoryRequest) {
        return Category.builder()
                .name(categoryRequest.getName())
                .description(categoryRequest.getDescription())
                .imageUrl(categoryRequest.getImageUrl())
                .build();
    }

    @Override
    public void updateEntity(CategoryRequest categoryRequest, Category entity) {
        setIfNotNull(categoryRequest.getName(), entity::setName);
        setIfNotNull(categoryRequest.getDescription(), entity::setDescription);
        setIfNotNull(categoryRequest.getImageUrl(), entity::setImageUrl);
    }
}
