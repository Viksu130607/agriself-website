package com.agriself.service;

import com.agriself.dto.*;
import com.agriself.model.User;
import com.agriself.model.UserRole;
import com.agriself.repository.UserRepository;
import com.agriself.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    // In-memory OTP cache for phone verification
    private final Map<String, String> otpCache = new ConcurrentHashMap<>();

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already registered: " + request.getPhoneNumber());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : UserRole.ROLE_FARMER)
                .email(request.getEmail())
                .location(request.getLocation() != null ? request.getLocation() : "Nashik, Maharashtra")
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtils.generateToken(saved.getPhoneNumber(), saved.getRole().name(), saved.getId(), saved.getFullName());

        return AuthResponse.builder()
                .token(token)
                .userId(saved.getId())
                .fullName(saved.getFullName())
                .phoneNumber(saved.getPhoneNumber())
                .role(saved.getRole())
                .message("User registered successfully as " + saved.getRole())
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new IllegalArgumentException("Invalid phone number or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid phone number or password");
        }

        String token = jwtUtils.generateToken(user.getPhoneNumber(), user.getRole().name(), user.getId(), user.getFullName());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .message("Login successful")
                .build();
    }

    public Map<String, String> sendOtp(String phoneNumber) {
        // Generate 6-digit OTP
        String otp = String.format("%06d", (int) (Math.random() * 900000) + 100000);
        otpCache.put(phoneNumber, otp);
        log.info("Agritech OTP generated for {}: {}", phoneNumber, otp);
        return Map.of(
                "phoneNumber", phoneNumber,
                "message", "OTP sent successfully to " + phoneNumber,
                "demoOtp", otp // Returned for test/demo ease
        );
    }

    @Transactional
    public AuthResponse verifyOtpAndLogin(String phoneNumber, String otp) {
        String cachedOtp = otpCache.get(phoneNumber);
        if (cachedOtp == null || !cachedOtp.equals(otp)) {
            // Allow master demo OTP '123456' for ease of testing
            if (!"123456".equals(otp)) {
                throw new IllegalArgumentException("Invalid or expired OTP");
            }
        }
        otpCache.remove(phoneNumber);

        // Find or auto-register farmer if first time
        User user = userRepository.findByPhoneNumber(phoneNumber).orElseGet(() -> {
            User newUser = User.builder()
                    .fullName("Farmer " + phoneNumber.substring(Math.max(0, phoneNumber.length() - 4)))
                    .phoneNumber(phoneNumber)
                    .password(passwordEncoder.encode("AgriSelf@" + phoneNumber.substring(Math.max(0, phoneNumber.length() - 4))))
                    .role(UserRole.ROLE_FARMER)
                    .location("Agricultural Hub, MH")
                    .build();
            return userRepository.save(newUser);
        });

        String token = jwtUtils.generateToken(user.getPhoneNumber(), user.getRole().name(), user.getId(), user.getFullName());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .message("OTP verified. Authenticated successfully.")
                .build();
    }

    public User getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + phoneNumber));
    }
}
