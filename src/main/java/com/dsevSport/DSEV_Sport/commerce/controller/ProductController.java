package com.dsevSport.DSEV_Sport.commerce.controller;

import com.dsevSport.DSEV_Sport.commerce.dto.request.ProductRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ApiResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ProductResponse;
import com.dsevSport.DSEV_Sport.commerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        return ResponseEntity.ok(
                ApiResponse.<List<ProductResponse>>builder()
                        .data(service.getAllProducts())
                        .message("Products retrieved successfully")
                        .success(true)
                        .build()
        );
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .data(service.getProductById(id))
                        .message("Product retrieved successfully")
                        .success(true)
                        .build()
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByCategory(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(
                ApiResponse.<List<ProductResponse>>builder()
                        .data(service.getProductsByCategory(categoryId))
                        .message("Products retrieved successfully")
                        .success(true)
                        .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProductsByName(@RequestParam String name) {
        return ResponseEntity.ok(
                ApiResponse.<List<ProductResponse>>builder()
                        .data(service.searchProductsByName(name))
                        .message("Products retrieved successfully")
                        .success(true)
                        .build()
        );
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getActiveProducts() {
        return ResponseEntity.ok(
                ApiResponse.<List<ProductResponse>>builder()
                        .data(service.getActiveProducts())
                        .message("Active products retrieved successfully")
                        .success(true)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .data(service.createProduct(request))
                        .message("Product created successfully")
                        .success(true)
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .data(service.updateProduct(id, request))
                        .message("Product updated successfully")
                        .success(true)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable UUID id) {
        service.deleteProduct(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Product deleted successfully")
                        .success(true)
                        .build()
        );
    }
}
