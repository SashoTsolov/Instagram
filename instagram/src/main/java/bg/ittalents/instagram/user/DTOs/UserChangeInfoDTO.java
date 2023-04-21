package bg.ittalents.instagram.user.DTOs;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserChangeInfoDTO {

    @Size(min = 1, max = 20, message = "Username must have minimum size of {min} and maximum size of {max} characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
            message = "Username should contain only alphanumeric characters, periods, underscores, and dashes")
    private String username;

    @Size(max = 20, message = "Name must have maximum size of {max} characters")
    @Pattern(regexp = "^[A-Za-z ]*$",
            message = "Name should contain only alphabets and spaces")
    private String name;

    @Size(max = 255, message = "Bio must have maximum size of {max} characters")
    private String bio;
}
