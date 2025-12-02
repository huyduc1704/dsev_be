package com.dsevSport.DSEV_Sport.commerce.controller;

import com.dsevSport.DSEV_Sport.commerce.dto.response.ApiResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ProductImageResponse;
import com.dsevSport.DSEV_Sport.commerce.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService service;

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> uploadImages(
            @PathVariable UUID id,
            @RequestPart("files") List<MultipartFile> files
    ) {
        return ResponseEntity.ok(
                ApiResponse.<List<ProductImageResponse>>builder()
                        .data(service.uploadImages(id, files))
                        .message("Upload successful")
                        .code(200)
                        .build()
        );
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> getImages(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.<List<ProductImageResponse>>builder()
                        .data(service.getByProduct(id))
                        .message("Get successful")
                        .code(200)
                        .build()
        );
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable UUID imageId) {
        service.deleteImages(imageId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Delete successful")
                        .code(200)
                        .build()
        );
    }
}
