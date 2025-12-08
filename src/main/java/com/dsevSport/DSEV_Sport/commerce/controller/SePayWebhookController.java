package com.dsevSport.DSEV_Sport.commerce.controller;

import com.dsevSport.DSEV_Sport.commerce.service.SePayService;
import com.dsevSport.DSEV_Sport.common.config.SePayConfig;
import com.dsevSport.DSEV_Sport.common.util.WebhookVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/sepay")
@RequiredArgsConstructor
public class SePayWebhookController {

    private final SePayService sePayService;
    private final SePayConfig config;

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestHeader Map<String, String> headers,
            @RequestBody String rawBody
    ) {
        log.info("=== SePay WEBHOOK RECEIVED ===");
        log.info("RAW BODY: {}", rawBody);
        log.info("HEADERS: {}", headers);

        // 1. Validate API KEY
        String apiKey = headers.getOrDefault("api-key", headers.get("x-api-key"));

        if (apiKey == null || !apiKey.equals(config.getWebhookApiKey())) {
            log.warn("Invalid or missing api-key");
            return ResponseEntity.status(401).body("Unauthorized");
        }

        // 2. Validate Signature
        String signature = headers.getOrDefault("signature", headers.get("x-signature"));

        if (!WebhookVerifier.verify(rawBody, signature, config.getWebhookApiKey())) {
            log.warn("Invalid signature");
            return ResponseEntity.status(401).body("Invalid signature");
        }

        // 3. Process webhook
        try {
            sePayService.processWebhook(rawBody);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Webhook processing failed", e);
            return ResponseEntity.status(500).body("Internal Error");
        }
    }
}
