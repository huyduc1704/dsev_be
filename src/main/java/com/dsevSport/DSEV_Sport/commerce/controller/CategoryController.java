package com.dsevSport.DSEV_Sport.commerce.controller;

import com.dsevSport.DSEV_Sport.commerce.dto.request.CategoryRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ApiResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.CategoryResponse;
import com.dsevSport.DSEV_Sport.commerce.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Categories", description = "Category management endpoints")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;

    @PermitAll
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(
                ApiResponse.<List<CategoryResponse>>builder()
                        .data(service.getAllCategories())
                        .message("Categories retrieved successfully")
                        .code(200)
                        .build()
        );
    }

    @PermitAll
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.<CategoryResponse>builder()
                        .data(service.getCategoryById(id))
                        .message("Category retrieved successfully")
                        .code(200)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestPart("request") CategoryRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(
                ApiResponse.<CategoryResponse>builder()
                        .data(service.createCategory(request, image))
                        .message("Category created successfully")
                        .code(200)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestPart("request") CategoryRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(
                ApiResponse.<CategoryResponse>builder()
                        .data(service.updateCategory(id, request, image))
                        .message("Category updated successfully")
                        .code(200)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID id) {
        service.deleteCategory(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Category deleted successfully")
                        .code(200)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CategoryResponse>> uploadCategoryImage(
            @PathVariable UUID id,
            @RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(
                ApiResponse.<CategoryResponse>builder()
                        .data(service.uploadCategoryImage(id, image))
                        .message("Category image uploaded successfully")
                        .code(200)
                        .build()
        );
    }
}
