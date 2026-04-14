package com.cdweb.bookstore.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
public class JwtProperties {
    private String base64Secret;
    private long accessTokenExpiration;   // ms
    private long refreshTokenExpiration;  // ms
}