package bg.ittalents.instagram.user.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserEmailDTO {

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    @Size(max = 50, message = "Email must have maximum size of {max} characters")
    private String email;
}
