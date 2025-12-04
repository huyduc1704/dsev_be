package com.dsevSport.DSEV_Sport.tryon.client;

import com.dsevSport.DSEV_Sport.tryon.config.FalProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FalCatVtonClient {

    private final FalProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public FalCatVtonResult generate(String userImageUrl, String garmentImageUrl) {

        log.info("FAL CONFIG apiKey={}, endpoint={}",
                properties.getApiKey(),
                properties.getCatVtonEndpoint()
        );

        try {
            Map<String, Object> body = Map.of(
                    "human_image_url", userImageUrl,
                    "garment_image_url", garmentImageUrl
            );

            String json = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getCatVtonEndpoint()))
                    .header("Authorization", "Key " + properties.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            log.info("Fal.ai status = {}", response.statusCode());
            log.info("Fal.ai body   = {}", response.body());

            if (response.statusCode() >= 400) {
                throw new RuntimeException("Fal.ai HTTP error " + response.statusCode());
            }

            Map<String, Object> resp =
                    objectMapper.readValue(response.body(), new TypeReference<>() {});

            String requestId = safeString(resp.get("request_id"));
            String status = safeString(resp.getOrDefault("status", "UNKNOWN"));
            String outputUrl = null;
            BigDecimal cost = null;

            if ("COMPLETED".equalsIgnoreCase(status)) {
                if (resp.containsKey("output")) {
                    Map<String, Object> output =
                            objectMapper.convertValue(resp.get("output"),
                                    new TypeReference<>() {});
                    outputUrl = safeString(output.get("image"));
                }
            }

            log.info("FAL CONFIG apiKey={}, endpoint={}",
                    properties.getApiKey(),
                    properties.getCatVtonEndpoint());

            if (resp.containsKey("metrics")) {
                Map<String, Object> metrics =
                        objectMapper.convertValue(resp.get("metrics"), new TypeReference<>() {});
                if (metrics.containsKey("cost")) {
                    cost = new BigDecimal(String.valueOf(metrics.get("cost")));
                }
            }

            return FalCatVtonResult.builder()
                    .status(status)
                    .requestId(requestId)
                    .outputImageUrl(outputUrl)
                    .estimatedCost(cost)
                    .rawResponse(response.body())
                    .build();

        } catch (Exception e) {
            log.error("Fal.ai CatVTON failed", e);

            return FalCatVtonResult.builder()
                    .status("FAILED")
                    .outputImageUrl(null)
                    .requestId(null)
                    .estimatedCost(null)
                    .rawResponse(e.getMessage())
                    .build();
        }
    }

    private String safeString(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }
}
