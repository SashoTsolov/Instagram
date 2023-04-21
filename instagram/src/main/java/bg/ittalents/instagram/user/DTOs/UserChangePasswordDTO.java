package bg.ittalents.instagram.user.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserChangePasswordDTO {

    @NotBlank(message = "Current password must not be blank")
    private String currentPassword;

    @NotBlank(message = "New password must not be blank")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{8,}$",
            message = "New password must contain at least 8 characters, including one uppercase letter," +
                    " one lowercase letter, one number and one special character")
    private String newPassword;

    @NotBlank(message = "Confirm new password must not be blank")
    private String confirmNewPassword;
}
