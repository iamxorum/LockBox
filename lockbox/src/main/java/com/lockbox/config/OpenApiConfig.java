package com.lockbox.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LockBox API")
                        .version("v1")
                        .description("API for LockBox Password Manager<br><br>" +
                                     "<h3>CSRF Protection</h3>" +
                                     "<p>All POST, PUT, and DELETE requests to the API require a CSRF token.</p>" +
                                     "<p>To obtain a CSRF token, make a GET request to <code>/api/test/csrf</code> and " +
                                     "include the returned token in your subsequent requests using one of these methods:</p>" +
                                     "<ul>" +
                                     "<li>Include the token in the <code>X-CSRF-TOKEN</code> header</li>" +
                                     "<li>Include the token as a <code>_csrf</code> parameter in the request body</li>" +
                                     "</ul>")
                        .contact(new Contact()
                                .name("LockBox Team")
                                .email("contact@lockbox.com")
                                .url("https://lockbox.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(Arrays.asList(
                        new Server().url("/").description("Default Server URL"),
                        new Server().url("http://localhost:8080").description("Development Server")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        .addSecuritySchemes("csrfToken", new SecurityScheme()
                                .name("X-CSRF-TOKEN")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("CSRF token for mutation operations")));
    }
    
    @Bean
    public OperationCustomizer customizeOperation() {
        return (operation, handlerMethod) -> {
            String httpMethod = operation.getOperationId().split("_")[0].toUpperCase();
            if (httpMethod.equals("POST") || httpMethod.equals("PUT") || httpMethod.equals("DELETE")) {
                operation.addSecurityItem(new SecurityRequirement().addList("csrfToken"));
                
                String description = operation.getDescription() != null ? operation.getDescription() : "";
                operation.setDescription(description + 
                                         "\n\n**Requires CSRF Token**: Include the token in the `X-CSRF-TOKEN` header " +
                                         "or as a `_csrf` parameter in the request body.");
            }
            return operation;
        };
    }
} 