package bg.ittalents.instagram.user;

import bg.ittalents.instagram.post.entities.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column
    private String username;
    @Column
    private String password;
    @Column
    private String email;
    @Column
    private String name;
    @Column
    private String bio;
    @Column
    private Date dateOfBirth;
    @Column
    private char gender;
    @Column
    private String profilePictureUrl;
    @Column
    private boolean isVerified;
    @Column
    private String verificationCode;
    @Column
    private Timestamp dateTimeCreated;
    @Column
    private boolean isDeactivated;
    @OneToMany(mappedBy = "owner")
    private List<Post> posts;

    @ManyToMany
    @JoinTable(
        name = "users_block_users",
        joinColumns = @JoinColumn(name = "blocked_user_id"),
        inverseJoinColumns = @JoinColumn(name = "blocking_user_id")
    )
    private Set<User> blocked = new HashSet<>();

}
