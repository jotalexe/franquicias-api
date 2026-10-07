package com.example.franquicias.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI franchiseApiOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Franchise API")
                        .description("API REST para la gestion de franquicias, sucursales y productos.")
                        .version("v1.0.0")
                        .contact(new Contact().name("Franchise API Team")));
    }
}
