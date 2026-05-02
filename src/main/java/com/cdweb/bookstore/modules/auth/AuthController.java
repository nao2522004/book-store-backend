package com.cdweb.bookstore.modules.auth;

import com.cdweb.bookstore.common.ApiResponse;
import com.cdweb.bookstore.modules.auth.dto.LoginRequest;
import com.cdweb.bookstore.modules.auth.dto.LoginResponse;
import com.cdweb.bookstore.modules.auth.dto.RegisterRequest;
import com.cdweb.bookstore.modules.auth.dto.RegisterResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
// @formatter:off
public class AuthController {

    private final AuthService authService;
    /**
     * POST /auth/login
     * → Access Token trả về JSON body
     * → Refresh Token set vào HttpOnly Cookie
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        LoginResponse data = authService.login(request, response);
        return ApiResponse.ok(data, "Đăng nhập thành công");
    }

    /**
     * POST /auth/refresh
     * → Đọc Refresh Token từ Cookie
     * → Trả về Access Token mới trong Body
     * → Xoay vòng Refresh Token (set Cookie mới)
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ApiResponse.unauthorized("Phiên làm việc hết hạn, vui lòng đăng nhập lại");
        }
        LoginResponse data = authService.refresh(refreshToken, response);
        return ApiResponse.ok(data, "Lấy Access Token mới thành công");
    }

    /**
     * POST /auth/logout
     * → Xoá Refresh Token khỏi DB + clear Cookie
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        authService.logout(refreshToken, response);
        return ApiResponse.ok(null, "Đăng xuất thành công");
    }

    /**
     * POST /auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        RegisterResponse user = authService.register(request);
        return ApiResponse.created(user, "Đăng ký tài khoản người dùng thành công");
    }
}