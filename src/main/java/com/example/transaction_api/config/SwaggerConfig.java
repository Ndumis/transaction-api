package com.example.transaction_api.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Transaction Aggregation API")
                        .description("API for aggregating customer financial transaction data")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Khayelihle Ndumiso Simelane")
                                .email("mkhayguze@gmail.com")
                                .url("https://github.com/Ndumis"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8484")
                                .description("Local Development Server")
                ));
    }
}
