package com.cdweb.bookstore.modules.auth;

import com.cdweb.bookstore.config.JwtProperties;
import com.cdweb.bookstore.config.JwtService;
import com.cdweb.bookstore.modules.auth.dto.LoginRequest;
import com.cdweb.bookstore.modules.auth.dto.LoginResponse;
import com.cdweb.bookstore.modules.auth.dto.RegisterRequest;
import com.cdweb.bookstore.modules.user.model.RefreshToken;
import com.cdweb.bookstore.modules.user.model.Role;
import com.cdweb.bookstore.modules.user.model.User;
import com.cdweb.bookstore.modules.user.repository.RefreshTokenRepository;
import com.cdweb.bookstore.modules.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ─── Login ────────────────────────────────────────────────────────────────

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        // 1. Xác thực credentials
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        // 2. Load user với roles
        User user = userRepository.findByEmailWithRoles(request.email()).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 3. Tạo Access Token → trả về body
        String accessToken = jwtService.buildAccessToken(auth, user);

        // 4. Tạo/xoay vòng Refresh Token → set HttpOnly Cookie
        String refreshTokenValue = jwtService.createOrRotateRefreshToken(user);
        jwtService.setRefreshTokenCookie(response, refreshTokenValue);

        return new LoginResponse(accessToken, "Bearer", jwtProperties.getAccessTokenExpiration() / 1000, user.getId(), user.getName(), user.getEmail());
    }

    // ─── Refresh ──────────────────────────────────────────────────────────────

    @Transactional
    public LoginResponse refresh(String cookieToken, HttpServletResponse response) {
        // 1. Tìm refresh token trong DB
        RefreshToken refreshToken = refreshTokenRepository.findByToken(cookieToken).orElseThrow(() -> new RuntimeException("Refresh token không hợp lệ"));

        // 2. Kiểm tra hết hạn
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            jwtService.clearRefreshTokenCookie(response);
            throw new RuntimeException("Refresh token đã hết hạn, vui lòng đăng nhập lại");
        }

        // 3. Build lại authentication từ user
        User user = refreshToken.getUser();
        List<GrantedAuthority> authorities = user.getRoles().stream().map(r -> (GrantedAuthority) new SimpleGrantedAuthority(r.getName())).toList();
        Authentication auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);

        // 4. Cấp Access Token mới
        String newAccessToken = jwtService.buildAccessToken(auth, user);

        // 5. Xoay vòng Refresh Token (Refresh Token Rotation)
        String newRefreshToken = jwtService.rotateRefreshToken(refreshToken);
        jwtService.setRefreshTokenCookie(response, newRefreshToken);

        return new LoginResponse(newAccessToken, "Bearer", jwtProperties.getAccessTokenExpiration() / 1000, user.getId(), user.getName(), user.getEmail());
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    @Transactional
    public void logout(String cookieToken, HttpServletResponse response) {
        if (cookieToken != null) {
            refreshTokenRepository.findByToken(cookieToken).ifPresent(refreshTokenRepository::delete);
        }
        jwtService.clearRefreshTokenCookie(response);
    }

    // ─── Register ─────────────────────────────────────────────────────────────

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email đã được sử dụng");
        }
        User user = User.builder().name(request.name()).email(request.email()).password(passwordEncoder.encode(request.password())).provider(User.Provider.LOCAL).roles(Set.of(Role.builder().id(1L).name("ROLE_USER").build())) // default role
                .build();
        userRepository.save(user);
        return user;
    }
}