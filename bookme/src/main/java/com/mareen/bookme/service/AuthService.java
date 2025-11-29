package com.mareen.bookme.service;

import com.mareen.bookme.dto.request.LoginRequest;
import com.mareen.bookme.dto.request.RegisterRequest;
import com.mareen.bookme.dto.request.ResendOtpRequest;
import com.mareen.bookme.dto.request.VerifyOtpRequest;
import com.mareen.bookme.dto.response.AuthResponse;
import com.mareen.bookme.dto.response.MessageResponse;
import com.mareen.bookme.entity.RefreshToken;
import com.mareen.bookme.entity.Role;
import com.mareen.bookme.entity.User;
import com.mareen.bookme.exception.EmailAlreadyExistsException;
import com.mareen.bookme.exception.UsernameAlreadyExistsException;
import com.mareen.bookme.mapper.UserMapper;
import com.mareen.bookme.repository.UserRepository;
import com.mareen.bookme.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.username());

        // Check if username already exists
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException("Username already exists: " + request.username());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already exists: " + request.email());
        }

        // Create new user
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER)
                .build();

        userRepository.save(user);

        // Generate and send OTP
        String otp = otpService.generateAndStoreOtp(request.email());
        emailService.sendOtpEmail(request.email(), otp);

        log.info("User registered successfully: {}", request.username());
        return new MessageResponse("Registration successful! Please check your email for OTP verification.");
    }

    @Transactional
    public MessageResponse verifyOtp(VerifyOtpRequest request) {
        log.info("Verifying OTP for email: {}", request.email());

        // Verify OTP
        otpService.verifyOtp(request.email(), request.otp());

        // Find user and mark email as verified
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.email()));

        user.markEmailAsVerified();
        userRepository.save(user);

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());

        log.info("Email verified successfully for user: {}", user.getUsername());

        return new MessageResponse("Email verified successfully! You can now login.");
    }

    @Transactional
    public MessageResponse resendOtp(ResendOtpRequest request) {
        log.info("Resending OTP for email: {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.email()));

        if (user.isEnabled()) {
            throw new IllegalStateException("Email is already verified");
        }

        // Delete old OTP if exists
        otpService.deleteOtp(request.email());

        // Generate and send new OTP
        String otp = otpService.generateAndStoreOtp(request.email());
        emailService.sendOtpEmail(request.email(), otp);

        log.info("OTP resent successfully to: {}", request.email());
        return new MessageResponse("OTP has been resent to your email");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.username());

        // Authenticate user
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException | DisabledException e) {
            log.error("Failed login attempt for username: {}", request.username());
            throw e;
        }

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.username()));

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());

        log.info("User logged in successfully: {}", request.username());

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                userMapper.mapToUserResponse(user)
        );
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenStr) {
        log.info("Refreshing access token");

        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenStr);
        User user = refreshToken.getUser();

        String newAccessToken = jwtService.generateAccessToken(user);

        log.info("Access token refreshed successfully for user: {}", user.getUsername());

        return new AuthResponse(
                newAccessToken,
                refreshTokenStr,
                userMapper.mapToUserResponse(user)
        );
    }

    @Transactional
    public MessageResponse logout(String refreshTokenStr) {
        log.info("User logout attempt");

        refreshTokenService.revokeRefreshToken(refreshTokenStr);

        log.info("User logged out successfully");
        return new MessageResponse("Logged out successfully");
    }
}
