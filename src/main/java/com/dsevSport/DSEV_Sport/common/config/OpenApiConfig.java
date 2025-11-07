package com.dsevSport.DSEV_Sport.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        List<Tag> orderTags = List.of(
                new Tag().name("Authentication").description("Authentication endpoints"),
                new Tag().name("Addresses").description("Address management endpoints"),
                new Tag().name("Categories").description("Category management endpoints"),
                new Tag().name("Products").description("Product management endpoints"),
                new Tag().name("Product Variants").description("Product variant management endpoints"),
                new Tag().name("Orders").description("Order management endpoints"),
                new Tag().name("Cart").description("Shopping cart management endpoints"),
                new Tag().name("Payments").description("Payment processing endpoints")
        );

        return new OpenAPI()
                .info(new Info().title("API").version("v1"))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .tags(orderTags);
    }

    @Bean
    public OpenApiCustomizer preserveAndOrderTagsCustomizer() {
        List<String> preferredOrder = List.of(
                "Authentication",
                "Addresses",
                "Categories",
                "Products",
                "Product Variants",
                "Orders",
                "Cart",
                "Payments",
                "User"
        );

        return openApi -> {
            List<Tag> existing = openApi.getTags();
            if (existing == null || existing.isEmpty()) return;

            Map<String, Tag> byName = new LinkedHashMap<>();
            for (String name : preferredOrder) {
                existing.stream().filter(t -> name.equals(t.getName())).findFirst()
                        .ifPresent(t -> byName.put(name, t));
            }
            for (Tag t : existing) {
                if (!byName.containsKey(t.getName())) byName.put(t.getName(), t);
            }
            openApi.setTags(new ArrayList<>(byName.values()));
        };
    }
}
