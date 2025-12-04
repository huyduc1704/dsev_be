package com.dsevSport.DSEV_Sport.commerce.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tryon_sessions")
public class TryonSession {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_image_id")
    ProductImage productImage;

    // ảnh input raw (link GCS)
    @Column(name = "image_input_url")
    private String imageInputUrl;

    // ảnh output (link GCS hoặc fal.ai)
    @Column(name = "output_result_url")
    private String outputResultUrl;

    @Column(name = "status")
    private String status; // PROCESSING, COMPLETED, FAILED

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_image_id")
    private UserImage inputImage;

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "prompt")
    private String prompt;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "estimated_cost")
    private BigDecimal estimatedCost;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "fail_reason")
    private String failReason;

    @Column(name = "retry_count")
    private Integer retryCount;
}
