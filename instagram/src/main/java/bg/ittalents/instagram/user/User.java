package bg.ittalents.instagram.user;

import bg.ittalents.instagram.comment.Comment;
import bg.ittalents.instagram.post.Post;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private long id;

    @Column(unique = true, length = 100)
    private String resetIdentifier;

    @Column
    private LocalDateTime resetIdentifierExpiry;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(length = 50)
    private String name;

    @Column
    private String bio;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(length = 1)
    private String gender;

    @Column
    private String profilePictureUrl;

    @Column(nullable = false)
    private boolean isVerified;

    @Column(unique = true, length = 100)
    private String verificationCode;

    @Column
    private LocalDateTime verificationCodeExpiry;

    @Column(nullable = false)
    private Timestamp dateTimeCreated;

    @Column(nullable = false)
    private Timestamp lastBeenOnline;

    @Column(nullable = false)
    private boolean isDeactivated;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_block_users",
            joinColumns = @JoinColumn(name = "blocking_user_id"),
            inverseJoinColumns = @JoinColumn(name = "blocked_user_id")
    )
    private Set<User> blocked = new HashSet<>();

    @ManyToMany(mappedBy = "blocked", fetch = FetchType.LAZY)
    private List<User> blockedBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "followers",
            joinColumns = @JoinColumn(name = "followed_user_id"),
            inverseJoinColumns = @JoinColumn(name = "following_user_id")
    )
    private Set<User> followers = new HashSet<>();

    @ManyToMany(mappedBy = "followers", fetch = FetchType.LAZY)
    private Set<User> following = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_like_posts",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    private List<Post> likedPosts;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_have_tagged_posts",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    private List<Post> taggedPosts;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "users_save_posts",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    private List<Post> savedPosts;

    @ManyToMany(fetch = FetchType.LAZY)
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
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
