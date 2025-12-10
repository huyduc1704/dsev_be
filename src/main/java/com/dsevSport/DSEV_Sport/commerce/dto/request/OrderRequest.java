package com.dsevSport.DSEV_Sport.commerce.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderRequest {
    UUID addressId;

    String fullName;
    String phoneNumber;
    String city;
    String ward;
    String street;

    String note;
}

