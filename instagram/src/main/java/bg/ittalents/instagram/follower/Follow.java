package bg.ittalents.instagram.follower;

import bg.ittalents.instagram.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

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

    // constructors, getters, setters, and other methods

}