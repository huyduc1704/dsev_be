package com.dsevSport.DSEV_Sport.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "sepay")
public class SePayConfig {
    private String webhookApiKey;
    private String bank;
    private String account;
}
