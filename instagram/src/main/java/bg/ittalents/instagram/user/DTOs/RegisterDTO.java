package bg.ittalents.instagram.user.DTOs;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class RegisterDTO {

        @NotBlank
        @Email
        @Size(max = 50)
        private String email;

        @NotBlank
        @Size(min = 2, max = 50)
        @Pattern(regexp = "[A-Za-z ]+", message = "Name should contain only alphabets and spaces")
        private String name;

        @NotBlank
        @Size(min = 4, max = 20)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "Username should contain only alphanumeric characters, periods, underscores, and dashes")
        private String username;

        @NotBlank
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}$",
                message = "Weak password")
        private String password;

        @NotBlank
        private String confirmPassword;

        private LocalDate dateOfBirth;
}
