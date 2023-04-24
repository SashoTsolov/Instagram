package bg.ittalents.instagram.follower;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FollowKey implements Serializable {

    @Column(name = "followed_user_id")
    private long followedUser;

    @Column(name = "following_user_id")
    private long followingUser;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FollowKey followKey = (FollowKey) o;
        return followedUser == followKey.followedUser && followingUser == followKey.followingUser;
    }

    @Override
    public int hashCode() {
        return Objects.hash(followedUser, followingUser);
    }
}
