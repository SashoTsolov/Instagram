package bg.ittalents.instagram.user.DTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserBasicInfoDTO {
    private long id;
    private String username;
    private String name;
    private String profilePictureUrl;
}
