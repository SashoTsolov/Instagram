package bg.ittalents.instagram.user;

import bg.ittalents.instagram.comment.Comment;
import bg.ittalents.instagram.post.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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
    private LocalDate dateOfBirth;
    @Column
    private String gender;
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
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;

    @ManyToMany
    @JoinTable(
        name = "users_block_users",
        joinColumns = @JoinColumn(name = "blocking_user_id"),
        inverseJoinColumns = @JoinColumn(name = "blocked_user_id")
    )
    private Set<User> blocked = new HashSet<>();

    @ManyToMany(mappedBy = "blocked")
    private List<User> blockedBy;

    @ManyToMany
    @JoinTable(
            name = "followers",
            joinColumns = @JoinColumn(name = "followed_user_id"),
            inverseJoinColumns = @JoinColumn(name = "following_user_id")
    )
    private Set<User> followers = new HashSet<>();

    @ManyToMany(mappedBy = "followers")
    private Set<User> following = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "users_like_posts",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    private List<Post> likedPosts;

    @ManyToMany
    @JoinTable(
            name = "users_have_tagged_posts",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    private List<Post> taggedPosts;

    @ManyToMany
    @JoinTable(
            name = "users_save_posts",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    private List<Post> savedPosts;

    @ManyToMany
    @JoinTable(
            name = "comments_have_likes",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "comment_id")
    )
    private List<Comment> likedComments;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
