package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.UpdateProfileRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.UserProfileResponse;

public interface UserService {
    UserProfileResponse getProfile(String username);
    UserProfileResponse updateProfile(String username, UpdateProfileRequest request);
}
