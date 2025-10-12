package com.rca.demo_course.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Demo Course Management API")
                        .description("A comprehensive REST API for managing students, courses, and grades in an educational system. " +
                                "This API provides CRUD operations for all entities with proper validation, error handling, and grading calculations.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Demo Course Team")
                                .email("support@democourse.com")
                                .url("https://github.com/democourse/api"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:9099")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.democourse.com")
                                .description("Production Server")
                ));
    }
}
