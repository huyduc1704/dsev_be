package com.dsevSport.DSEV_Sport.commerce.service;

import java.util.Map;

public interface SePayService {
    void processWebhook(String rawJson);
}
