package com.dsevSport.DSEV_Sport.commerce.controller;

import com.dsevSport.DSEV_Sport.commerce.dto.request.OrderRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.request.UpdateOrderStatusRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ApiResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.OrderResponse;
import com.dsevSport.DSEV_Sport.commerce.service.OrderService;
import com.dsevSport.DSEV_Sport.common.util.enums.OrderStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Orders", description = "Order management endpoints")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            Authentication authentication,
            @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<OrderResponse>builder()
                        .message("Order created successfully")
                        .data(orderService.createOrderFromCart(authentication.getName(), request))
                        .code(201)
                        .build()
        );
    }

    @GetMapping("/me/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(Authentication authentication) {
        return ResponseEntity.ok(
                ApiResponse.<List<OrderResponse>>builder()
                        .message("Orders retrieved successfully")
                        .data(orderService.getUserOrders(authentication.getName()))
                        .code(200)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @PatchMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>builder()
                        .message("Order status updated successfully")
                        .data(orderService.updateOrderStatus(orderId, request.getStatus()))
                        .code(200)
                        .build()
        );
    }

    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<Void> cancelOrder(
            Authentication authentication,
            @PathVariable UUID orderId) {
        orderService.cancelOrder(authentication.getName(), orderId);
        return ResponseEntity.noContent().build();
    }
}
