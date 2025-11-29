package com.mareen.bookme.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendOtpRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email
) {
}
