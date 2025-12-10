package com.dsevSport.DSEV_Sport.commerce.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TagRequest {
    String name;
    String displayName; // Nam, Nữ, Trẻ em
}
