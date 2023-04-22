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

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    @Size(max = 50, message = "Email must have maximum size of {max} characters")
    private String email;

    @NotBlank(message = "Name must not be blank")
    @Size(min = 2, max = 50, message = "Name must have minimum size of {min} and maximum size of {max} characters")
    @Pattern(regexp = "[A-Za-z ]+", message = "Name should contain only alphabets and spaces")
    private String name;

    @NotBlank(message = "Username must not be blank")
    @Size(min = 4, max = 20, message = "Username must have minimum size of {min} and maximum size of {max} characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
            message = "Username should contain only alphanumeric characters, periods, underscores, and dashes")
    private String username;

    @NotBlank(message = "Password must not be blank")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Password must contain at least 8 characters, including one uppercase letter, " +
                    "one lowercase letter, one number and one special character")
    private String password;

    @NotBlank(message = "Confirm password must not be blank")
    private String confirmPassword;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be a date in the past")
    private LocalDate dateOfBirth;
}
