package com.dsevSport.DSEV_Sport.commerce.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    Category category;

    @Column(name = "name", nullable = false)
    String name;
    String description;
    String brand;

    @Column(name = "image_url")
    String imageUrl;

    @Column(name = "is_active")
    Boolean isActive;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Column(name = "created_at")
    LocalDateTime createdAt;
}
