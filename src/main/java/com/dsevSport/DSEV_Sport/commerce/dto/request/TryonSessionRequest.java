package com.dsevSport.DSEV_Sport.commerce.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TryonSessionRequest {
    UUID productImageId;     // ảnh sản phẩm muốn thử
    UUID userImageId;   // ảnh người dùng đã upload (UserImage.id)
}
