package com.dsevSport.DSEV_Sport.commerce.controller;

import com.dsevSport.DSEV_Sport.commerce.dto.request.TryonSessionRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ApiResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.TryonSessionResponse;
import com.dsevSport.DSEV_Sport.commerce.service.TryonSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Try-on", description = "AI Try-on endpoints")
@RestController
@RequestMapping("/api/v1/tryon")
@RequiredArgsConstructor
public class TryonSessionController {

    private final TryonSessionService tryonSessionService;

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<TryonSessionResponse>> createSession(
            Authentication authentication,
            @RequestBody TryonSessionRequest request) {

        TryonSessionResponse response =
                tryonSessionService.createTryonSession(authentication.getName(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<TryonSessionResponse>builder()
                        .code(201)
                        .message("Try-on session created")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<TryonSessionResponse>>> mySessions(
            Authentication authentication) {

        List<TryonSessionResponse> data =
                tryonSessionService.getMySessions(authentication.getName());

        return ResponseEntity.ok(
                ApiResponse.<List<TryonSessionResponse>>builder()
                        .code(200)
                        .message("Try-on sessions retrieved")
                        .data(data)
                        .build()
        );
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<TryonSessionResponse>> getSession(
            Authentication authentication,
            @PathVariable UUID id) {

        TryonSessionResponse response =
                tryonSessionService.getMyTryonSession(authentication.getName(), id);

        return ResponseEntity.ok(
                ApiResponse.<TryonSessionResponse>builder()
                        .code(200)
                        .message("Try-on session retrieved")
                        .data(response)
                        .build()
        );
    }
}
