package bg.ittalents.instagram.user.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserWithoutPassAndEmailDTO {
    private long id;
    private String username;
    private String name;
    private String bio;
    private String profilePictureUrl;
    private int numFollowers;
    private int numFollowing;
    private int numPosts;

    public UserWithoutPassAndEmailDTO(final long id, final String name, final String username,
                                      final String profilePictureUrl, final int numFollowers,
                                      final int numFollowing, final int numPosts, final String bio) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.profilePictureUrl = profilePictureUrl;
        this.numFollowers = numFollowers;
        this.numFollowing = numFollowing;
        this.numPosts = numPosts;
        this.bio = bio;
    }
}
