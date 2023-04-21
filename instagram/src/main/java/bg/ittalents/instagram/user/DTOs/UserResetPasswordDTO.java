package bg.ittalents.instagram.user.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserResetPasswordDTO {

    @NotBlank(message = "Password must not be blank")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Weak password")
    private String newPassword;

    @NotBlank(message = "Password must not be blank")
    private String confirmPassword;
}
