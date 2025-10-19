package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.OrderRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.OrderResponse;
import com.dsevSport.DSEV_Sport.common.util.enums.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse createOrderFromCart(String username, OrderRequest request);
    List<OrderResponse> getUserOrders(String username);
    OrderResponse updateOrderStatus(UUID orderId, OrderStatus status);
    void cancelOrder(String username, UUID orderId);
}
