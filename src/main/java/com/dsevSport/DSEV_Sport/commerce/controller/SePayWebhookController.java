package com.dsevSport.DSEV_Sport.commerce.controller;

import com.dsevSport.DSEV_Sport.commerce.service.SePayService;
import com.dsevSport.DSEV_Sport.common.config.SePayConfig;
import com.dsevSport.DSEV_Sport.common.util.WebhookVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/sepay")
@RequiredArgsConstructor
public class SePayWebhookController {

    private final SePayService sePayService;
    private final SePayConfig config;

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestHeader("api-key") String apiKey,
            @RequestHeader(value = "signature", required = false) String signature,
            @RequestBody String rawBody
    ) {
       log.info("Received SePay Webhook raw body: {}", rawBody);

       if(!config.getWebhookApiKey().equals(apiKey)) {
           log.warn("Invalid API key");
           return ResponseEntity.status(401).body("Unauthorized");
       }

       if (!WebhookVerifier.verify(rawBody, signature, config.getWebhookApiKey())) {
           log.warn("Invalid HMAC signature");
           return ResponseEntity.status(401).body("Invalid signature");
       }

        try {
            sePayService.processWebhook(rawBody);
        } catch (Exception e) {
            log.error("Webhook processing failed", e);
            return ResponseEntity.status(500).body("Internal Error");
        }

       return ResponseEntity.ok("OK");

    }
}
