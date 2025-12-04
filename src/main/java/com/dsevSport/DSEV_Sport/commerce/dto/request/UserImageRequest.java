package com.dsevSport.DSEV_Sport.commerce.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserImageRequest {
    Boolean isPrimary;
    String source;
}
