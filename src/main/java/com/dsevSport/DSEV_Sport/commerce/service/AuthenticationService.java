package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.LoginRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.request.RegisterRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse register(RegisterRequest registerRequest);
    AuthenticationResponse login(LoginRequest loginRequest);
}
