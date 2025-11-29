package com.mareen.bookme.service;

import com.mareen.bookme.exception.InvalidOtpException;
import com.mareen.bookme.exception.OtpExpiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String OTP_PREFIX = "otp:";
    private static final SecureRandom random = new SecureRandom();

    @Value("${otp.expiration}")
    private long otpExpiration;

    @Value("${otp.length}")
    private int otpLength;

    public String generateAndStoreOtp(String email) {
        String otp = generateOtp();
        String key = OTP_PREFIX + email;

        // Store OTP in Redis with expiration
        redisTemplate.opsForValue().set(key, otp, otpExpiration, TimeUnit.SECONDS);

        return otp;
    }

    public void verifyOtp(String email, String otp) {
        String key = OTP_PREFIX + email;
        String storedOtp = (String) redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            throw new OtpExpiredException("OTP has expired. Please request a new one.");
        }

        if (!storedOtp.equals(otp)) {
            throw new InvalidOtpException("Invalid OTP provided.");
        }

        // Delete OTP after successful verification
        redisTemplate.delete(key);
    }

    public void deleteOtp(String email) {
        String key = OTP_PREFIX + email;
        redisTemplate.delete(key);
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    public boolean otpExists(String email) {
        String key = OTP_PREFIX + email;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
