package bg.ittalents.instagram.post;

import bg.ittalents.instagram.hashtag.Hashtag;
import bg.ittalents.instagram.location.Location;
import bg.ittalents.instagram.media.Media;
import bg.ittalents.instagram.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(nullable = false)
    private Boolean isCreated;

    @Column(name = "caption", columnDefinition = "TEXT")
    private String caption;

    @Column(name = "date_time_created", nullable = false)
    private LocalDateTime dateTimeCreated;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "posts_have_hashtags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    private Set<Hashtag> hashtags;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Media> mediaUrls = new ArrayList<>();

    @ManyToMany(mappedBy = "likedPosts", fetch = FetchType.LAZY)
    private List<User> likedBy;

    @ManyToMany(mappedBy = "taggedPosts", fetch = FetchType.LAZY)
    private List<User> taggedUsers;

    @ManyToMany(mappedBy = "savedPosts", fetch = FetchType.LAZY)
    private List<User> savedBy;
}

