package bg.ittalents.instagram.follower;

import bg.ittalents.instagram.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity(name = "followers")
public class Follow {

    @EmbeddedId
    private FollowKey id;

    @MapsId("followedUser")
    @ManyToOne
    @JoinColumn(name = "followed_user_id")
    private User followedUser;

    @MapsId("followingUser")
    @ManyToOne
    @JoinColumn(name = "following_user_id")
    private User followingUser;

    @Column(name = "date_time_of_follow")
    private Timestamp dateTimeOfFollow;
}