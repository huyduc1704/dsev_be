package com.dsevSport.DSEV_Sport.commerce.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserImageResponse {
    UUID id;
    String imageUrl;
    String fileName;
    Integer fileSize;
    Integer width;
    Integer height;
    String format;
    Boolean isPrimary;
    String source;
    LocalDateTime createdAt;
}
