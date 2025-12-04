package com.dsevSport.DSEV_Sport.tryon.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "fal")
public class FalProperties {

    private String apiKey;

    private CatVton catVton = new CatVton();

    @Data
    public static class CatVton {
        private String modelName;
        private String endpoint;
    }

    public String getCatVtonModelName() {
        return catVton.getModelName();
    }

    public String getCatVtonEndpoint() {
        return catVton.getEndpoint();
    }
}