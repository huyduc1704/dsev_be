package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.CategoryRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.CategoryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();
    CategoryResponse getCategoryById(UUID id);
    CategoryResponse createCategory(CategoryRequest request, MultipartFile image);
    CategoryResponse updateCategory(UUID id, CategoryRequest request, MultipartFile image);
    void deleteCategory(UUID id);
    CategoryResponse uploadCategoryImage(UUID id, MultipartFile image);
}
