package bg.ittalents.instagram.user.DTOs;

import bg.ittalents.instagram.post.entities.Post;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserWithoutPassAndEmailDTO {

    private long id;
    private String username;
    private String name;
    private String bio;
    private String profilePictureUrl;
    private List<Post> posts;
}
