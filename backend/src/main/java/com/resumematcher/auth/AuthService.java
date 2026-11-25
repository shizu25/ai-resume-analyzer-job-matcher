package com.resumematcher.auth;

import com.resumematcher.auth.dto.*;
import com.resumematcher.security.JwtUtils;
import com.resumematcher.user.User;
import com.resumematcher.user.UserProfile;
import com.resumematcher.user.UserProfileRepository;
import com.resumematcher.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       UserProfileRepository userProfileRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER");
        User savedUser = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        profile.setFullName(request.getFullName() != null ? request.getFullName().trim() : null);
        userProfileRepository.save(profile);

        String token = jwtUtils.generateTokenFromEmail(savedUser.getEmail(), savedUser.getId(), savedUser.getRole());
        String refreshToken = jwtUtils.generateRefreshToken(savedUser.getEmail(), savedUser.getId());

        return new AuthResponse(savedUser.getId(), savedUser.getEmail(), profile.getFullName(), savedUser.getRole(), token, refreshToken);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        String fullName = profile != null ? profile.getFullName() : null;

        String token = jwtUtils.generateTokenFromEmail(user.getEmail(), user.getId(), user.getRole());
        String refreshToken = jwtUtils.generateRefreshToken(user.getEmail(), user.getId());

        return new AuthResponse(user.getId(), user.getEmail(), fullName, user.getRole(), token, refreshToken);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtUtils.validateJwtToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        String email = jwtUtils.getEmailFromJwtToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);
        String fullName = profile != null ? profile.getFullName() : null;

        String newToken = jwtUtils.generateTokenFromEmail(user.getEmail(), user.getId(), user.getRole());
        return new AuthResponse(user.getId(), user.getEmail(), fullName, user.getRole(), newToken, refreshToken);
    }
}
