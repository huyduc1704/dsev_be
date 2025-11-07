package com.dsevSport.DSEV_Sport.commerce.controller;

import com.dsevSport.DSEV_Sport.commerce.dto.request.UpdateProfileRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ApiResponse;
import com.dsevSport.DSEV_Sport.commerce.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "User management endpoints")
@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Profile retrieved successfully")
                        .data(userService.getProfile(authentication.getName()))
                        .code(200)
                        .build()
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Object>> updateMyProfile(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .message("Profile updated successfully")
                        .data(userService.updateProfile(authentication.getName(), request))
                        .code(200)
                        .build()
        );
    }
}
