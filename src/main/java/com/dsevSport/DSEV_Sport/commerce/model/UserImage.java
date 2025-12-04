package com.dsevSport.DSEV_Sport.commerce.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "user_images")
public class UserImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "image_url", nullable = false)
    String imageUrl;

    @Column(name = "file_name")
    String fileName;

    @Column(name = "file_size")
    Integer fileSize;

    @Column(name = "width")
    Integer width;

    @Column(name = "height")
    Integer height;

    @Column(name = "format")
    String format;

    @Column(name = "is_primary")
    Boolean isPrimary = false;

    @Column(name = "source", length = 50)
    String source;

    @Column(name = "created_at")
    LocalDateTime createdAt;
}
