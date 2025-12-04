package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.TryonSessionRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.TryonSessionResponse;

import java.util.List;
import java.util.UUID;

public interface TryonSessionService {

    TryonSessionResponse createTryonSession(String username, TryonSessionRequest request);

    TryonSessionResponse getMyTryonSession(String username, UUID sessionId);

    List<TryonSessionResponse> getMySessions(String username);
}
