package com.dsevSport.DSEV_Sport.commerce.controller;

import com.dsevSport.DSEV_Sport.commerce.dto.request.UserImageRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ApiResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.UserImageResponse;
import com.dsevSport.DSEV_Sport.commerce.service.UserImageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "User Images", description = "User image upload for try-on")
@RestController
@RequestMapping("/api/v1/me/images")
@RequiredArgsConstructor
public class UserImageController {

    private final UserImageService userImageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserImageResponse>> uploadUserImage(
            Authentication authentication,
            @RequestPart("file") MultipartFile file
    ) {

        UserImageResponse response =
                userImageService.uploadUserImage(authentication.getName(), file);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<UserImageResponse>builder()
                        .code(201)
                        .message("User image uploaded successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserImageResponse>>> getMyImages(
            Authentication authentication) {

        List<UserImageResponse> data =
                userImageService.getMyImages(authentication.getName());

        return ResponseEntity.ok(
                ApiResponse.<List<UserImageResponse>>builder()
                        .code(200)
                        .message("User images retrieved successfully")
                        .data(data)
                        .build()
        );
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteMyImage(
            Authentication authentication,
            @PathVariable UUID imageId) {

        userImageService.deleteMyImage(authentication.getName(), imageId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{imageId}/primary")
    public ResponseEntity<Void> setPrimaryImage(
            Authentication authentication,
            @PathVariable UUID imageId) {

        userImageService.setPrimaryImage(authentication.getName(), imageId);
        return ResponseEntity.noContent().build();
    }
}
