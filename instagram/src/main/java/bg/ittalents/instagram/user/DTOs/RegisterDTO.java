package bg.ittalents.instagram.user.DTOs;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterDTO(
        @NotBlank
        @Email
        @Size(max = 50)
        String email,
        @NotBlank
        @Size(min = 2, max = 50)
        @Pattern(regexp = "[A-Za-z ]+", message = "Name should contain only alphabets and spaces")
        String name,
        @NotBlank
        @Size(min = 4, max = 20)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "Username should contain only alphanumeric characters, periods, underscores, and dashes")
        String username,
        @NotBlank
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
                message = "Weak password")
        String password,
        @NotBlank
        String confirmPassword,
        LocalDate dateOfBirth
) {
}
