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

        @Size(min = 1, max = 20)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "Username should contain only alphanumeric characters, periods, underscores, and dashes")
        private String username;

        @Size(max = 20)
        @Pattern(regexp = "^[A-Za-z ]*$",
                message = "name should contain only alphanumeric characters, periods, underscores, and dashes")
        private String name;

        private String bio;
}
