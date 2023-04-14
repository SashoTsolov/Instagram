package bg.ittalents.instagram.user.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserForgotPasswordDTO(
        @NotBlank
        @Email
        String email
) {}
