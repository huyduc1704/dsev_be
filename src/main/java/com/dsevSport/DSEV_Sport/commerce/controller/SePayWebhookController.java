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

    /**
     * SePay may send Authorization header as:
     *   Authorization: Apikey <KEY>
     * or custom header names. We accept either.
     *
     * Signature header is optional (depending on your SePay settings).
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader Map<String, String> headers,
            @RequestBody String rawBody
    ) {
        log.info("=== SePay WEBHOOK RECEIVED ===");
        log.info("RAW BODY: {}", rawBody);
        log.info("HEADERS: {}", headers);

        // 1) read api key from Authorization or headers "api-key"/"x-api-key"
        String auth = headers.getOrDefault("authorization", headers.getOrDefault("Authorization", ""));
        String apiKey = null;
        if (auth != null && !auth.isBlank()) {
            String low = auth.toLowerCase();
            if (low.startsWith("apikey ")) {
                apiKey = auth.substring(7).trim();
            } else if (low.startsWith("bearer ")) {
                apiKey = auth.substring(7).trim(); // defensive
            }
        }
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = headers.getOrDefault("api-key", headers.getOrDefault("x-api-key", null));
        }

        if (apiKey == null || !apiKey.equals(config.getWebhookApiKey())) {
            log.warn("Invalid or missing api-key. received={}, expected present? {}", apiKey, config.getWebhookApiKey() != null);
            // Return 401 so SePay knows webhook not authorized
            return ResponseEntity.status(401).body("Unauthorized");
        }

        // 2) signature validation (optional)
        String signature = headers.getOrDefault("signature", headers.getOrDefault("x-signature", ""));
        if (signature != null && !signature.isEmpty()) {
            boolean ok = WebhookVerifier.verify(rawBody, signature, config.getWebhookApiKey());
            if (!ok) {
                log.warn("Invalid HMAC signature");
                return ResponseEntity.status(401).body("Invalid signature");
            }
        } else {
            log.debug("No signature provided; skipping signature verification");
        }

        // 3) process webhook — ensure any exception is caught here so we return 200/202/500 accordingly
        try {
            sePayService.processWebhook(rawBody);
            // Return 200 quickly (SePay expects 200)
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Webhook processing failed", e);
            // Return 500 only if internal error occurred (so SePay may retry)
            return ResponseEntity.status(500).body("Internal Error");
        }
    }
}
