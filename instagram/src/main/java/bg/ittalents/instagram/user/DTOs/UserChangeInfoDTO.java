package bg.ittalents.instagram.user.DTOs;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserChangeInfoDTO(
        @Size(min = 4, max = 20)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "Username should contain only alphanumeric characters, periods, underscores, and dashes")
        String username,
        @Size(min = 4, max = 20)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "Username should contain only alphanumeric characters, periods, underscores, and dashes")
        String name,
        @Size(min = 1, max = 255)
        String bio
) {}
