package com.dsevSport.DSEV_Sport.commerce.dto.request;

import com.dsevSport.DSEV_Sport.common.util.UserRole;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    String username;
    String email;
    String password;
    String phoneNumber;
    UserRole role;
    List<AddressRequest> addresses;
}
