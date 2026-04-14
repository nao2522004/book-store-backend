package com.cdweb.bookstore.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {

    private SecurityScheme createBearerScheme() {
        // @formatter:off
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("Nhập Access Token vào đây để gọi các API cần quyền truy cập.");
        // @formatter:on
    }

    private Server createServer(String url, String description) {
        Server server = new Server();
        server.setUrl(url);
        server.setDescription(description);
        return server;
    }

    private Contact createContact() {
        // @formatter:off
        return new Contact()
                .email("your-email@example.com") // Thay bằng email của bạn
                .name("Bookstore Admin Team")
                .url("https://yourbookstore.vn"); // Website dự án của bạn
        // @formatter:on
    }

    private License createLicense() {
        return new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");
    }

    private Info createApiInfo() {
        // @formatter:off
        return new Info()
                .title("Bookstore API Documentation") // Tên dự án của bạn
                .version("1.0.0")
                .contact(createContact())
                .description("Tài liệu API chi tiết cho hệ thống quản lý cửa hàng sách (Backend Services).")
                .termsOfService("https://yourbookstore.vn/terms")
                .license(createLicense());
        // @formatter:on
    }

    @Bean
    OpenAPI myOpenAPI() {
        // @formatter:off
        return new OpenAPI()
                .info(createApiInfo())
                .servers(List.of(
                        createServer("http://localhost:8080",
                                "Môi trường Phát triển (Development)"),
                        createServer("https://api-staging.bookstore.com",
                                "Môi trường Kiểm thử (Testing)"),
                        createServer("https://api.bookstore.com",
                                "Môi trường Thực tế (Production)")))
                .addSecurityItem(
                        new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createBearerScheme()));
        // @formatter:on
    }
}