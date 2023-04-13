package bg.ittalents.instagram.user.DTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class RegisterDTO {

    private String email;
    private String name;
    private String username;
    private String password;
    private String confirmPassword;
    private Date dateOfBirth;
}
