package com.cdweb.bookstore.config;

import com.cdweb.bookstore.modules.user.model.RefreshToken;
import com.cdweb.bookstore.modules.user.model.User;
import com.cdweb.bookstore.modules.user.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;
    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;

    public String buildAccessToken(Authentication auth, User user) {
        Instant now = Instant.now();

        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("bookstore")
                .issuedAt(now)
                .expiresAt(now.plusMillis(jwtProperties.getAccessTokenExpiration()))
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("roles", roles)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public String createOrRotateRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration());

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .map(rt -> {
                    rt.setToken(tokenValue);
                    rt.setExpiryDate(expiry);
                    return rt;
                })
                .orElseGet(() -> RefreshToken.builder()
                        .user(user)
                        .token(tokenValue)
                        .expiryDate(expiry)
                        .build());

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    public String rotateRefreshToken(RefreshToken refreshToken) {
        String newValue = UUID.randomUUID().toString();
        refreshToken.setToken(newValue);
        refreshToken.setExpiryDate(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration()));
        refreshTokenRepository.save(refreshToken);
        return newValue;
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String tokenValue) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokenValue)
                .httpOnly(true)
                .secure(true)           // Đổi thành false khi test local HTTP
                .path("/auth/refresh")  // Chỉ gửi cookie cho endpoint này
                .maxAge(jwtProperties.getRefreshTokenExpiration() / 1000)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/auth/refresh")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
