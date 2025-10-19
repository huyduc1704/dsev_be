package com.dsevSport.DSEV_Sport.commerce.repository;

import com.dsevSport.DSEV_Sport.commerce.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    // Query orders của user theo username, sắp xếp giảm dần
    List<Order> findByUser_UsernameOrderByCreatedAtDesc(String username);

    List<Order> findByUser_EmailOrderByCreatedAtDesc(String email);

    // Query order theo order number
    Optional<Order> findByOrderNumber(String orderNumber);
}
