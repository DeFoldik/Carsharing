package com.carsharing.carsharing.controller;

import com.carsharing.carsharing.model.Owner;
import com.carsharing.carsharing.model.Renter;
import com.carsharing.carsharing.model.User;
import com.carsharing.carsharing.repository.UserRepository;
import com.carsharing.carsharing.security.JwtUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Добавлен внутренний класс AuthResponse
    @Data
    public static class AuthResponse {
        private final String token;
        private final String userType;

        public AuthResponse(String token, String userType) {
            this.token = token;
            this.userType = userType;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        // Сначала находим пользователя для проверки существования
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Затем аутентифицируем (проверка пароля происходит внутри authenticationManager)
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String role = user instanceof Owner ? "OWNER" : "RENTER";
        String token = jwtUtil.generateToken(request.getUsername(), role);
        String userType = user instanceof Owner ? "OWNER" : "RENTER";
        return ResponseEntity.ok(new AuthResponse(token, userType));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        Optional<User> existing = userRepository.findByUsername(request.getUsername());
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User user;
        if ("OWNER".equalsIgnoreCase(request.getRole())) {
            user = new Owner(request.getUsername(), passwordEncoder.encode(request.getPassword()), request.getName());
        } else if ("RENTER".equalsIgnoreCase(request.getRole())) {
            user = new Renter(request.getUsername(), passwordEncoder.encode(request.getPassword()), request.getName());
        } else {
            return ResponseEntity.badRequest().body("Invalid role: must be OWNER or RENTER");
        }

        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfileDto profileDto = new UserProfileDto();
        profileDto.setId(user.getId());
        profileDto.setUsername(user.getUsername());
        profileDto.setName(user.getName());
        profileDto.setUserType(user instanceof Owner ? "OWNER" : "RENTER");

        return ResponseEntity.ok(profileDto);
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверка текущего пароля (используем getPasswordHash())
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Неверный текущий пароль");
        }

        // Обновление данных
        user.setName(request.getName());

        // Если нужно обновить пароль (новый пароль предоставлен)
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        userRepository.save(user);

        return ResponseEntity.ok("Профиль успешно обновлен");
    }

    // Внутренние DTO классы
    @Data
    static class UserProfileDto {
        private Long id;
        private String username;
        private String name;
        private String userType;
    }

    @Data
    static class AuthRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }

    @Data
    static class RegisterRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        private String name;
        private String role;
    }

    @Data
    static class UpdateProfileRequest {
        @NotBlank
        private String name;
        @NotBlank
        private String currentPassword;
        private String newPassword;
    }
}