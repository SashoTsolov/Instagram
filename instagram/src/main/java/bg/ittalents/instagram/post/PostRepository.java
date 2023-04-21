package bg.ittalents.instagram.post;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByIdAndIsCreatedIsFalse(long id);


    @Query(value = """
            SELECT p.*
            FROM posts p
            JOIN users u ON p.owner_id = u.id
            LEFT JOIN users_block_users ubu1 ON ubu1.blocked_user_id = p.owner_id AND ubu1.blocking_user_id = :userId
            LEFT JOIN users_block_users ubu2 ON ubu2.blocking_user_id = p.owner_id AND ubu2.blocked_user_id = :userId
            WHERE p.id = :postId AND p.is_created = 1
            AND u.is_deactivated = 0
            AND ubu1.blocked_user_id IS NULL
            AND ubu2.blocking_user_id IS NULL
            """, nativeQuery = true)
    Optional<Post> findByIdAndIsCreatedIsTrue(long userId, long postId);

    @Query(value = """
            SELECT p.*
            FROM posts p
            JOIN users u ON p.owner_id = u.id
            LEFT JOIN users_block_users ubu1 ON ubu1.blocked_user_id = p.owner_id AND ubu1.blocking_user_id = :userId
            LEFT JOIN users_block_users ubu2 ON ubu2.blocking_user_id = p.owner_id AND ubu2.blocked_user_id = :userId
            WHERE p.owner_id = :ownerId AND p.is_created = 1
            AND u.is_deactivated = 0
            AND ubu1.blocked_user_id IS NULL
            AND ubu2.blocking_user_id IS NULL
            ORDER BY p.date_time_created DESC
            """, nativeQuery = true)
    Slice<Post> findByOwnerIdAndIsCreatedIsTrueOrderByDateTimeCreatedDesc(long userId, long ownerId, Pageable pageable);


    @Query(value = """
            SELECT p.* 
            FROM posts p
            JOIN users u ON p.owner_id = u.id
            JOIN posts_have_hashtags phh ON p.id = phh.post_id 
            JOIN hashtags h ON phh.hashtag_id = h.id 
            LEFT JOIN users_block_users ubu1 ON ubu1.blocked_user_id = p.owner_id AND ubu1.blocking_user_id = :userId
            LEFT JOIN users_block_users ubu2 ON ubu2.blocking_user_id = p.owner_id AND ubu2.blocked_user_id = :userId
            WHERE h.name = :hashtagName AND p.is_created = 1
            AND u.is_deactivated = 0
            AND ubu1.blocked_user_id IS NULL
            AND ubu2.blocking_user_id IS NULL
            ORDER BY p.date_time_created DESC
            """, nativeQuery = true)
    Slice<Post> findAllByHashtagNameSortedByDateTimeCreatedDesc(long userId, String hashtagName, Pageable pageable);


    @Query(value = """
            SELECT p.* 
            FROM posts p
            JOIN users u ON p.owner_id = u.id
            JOIN locations l ON p.location_id = l.id 
            LEFT JOIN users_block_users ubu1 ON ubu1.blocked_user_id = p.owner_id AND ubu1.blocking_user_id = :userId
            LEFT JOIN users_block_users ubu2 ON ubu2.blocking_user_id = p.owner_id AND ubu2.blocked_user_id = :userId
            WHERE l.name = :locationName AND p.is_created = 1
            AND u.is_deactivated = 0
            AND ubu1.blocked_user_id IS NULL
            AND ubu2.blocking_user_id IS NULL
            ORDER BY p.date_time_created DESC
            """, nativeQuery = true)
    Slice<Post> findAllByLocationNameSortedByDateTimeCreatedDesc(long userId, String locationName, Pageable pageable);

    @Query(value = """
            SELECT p.*
            FROM posts p
            JOIN users u ON p.owner_id = u.id
            JOIN users_save_posts usp ON p.id = usp.post_id
            LEFT JOIN users_block_users ubu1 ON ubu1.blocked_user_id = p.owner_id AND ubu1.blocking_user_id = :userId
            LEFT JOIN users_block_users ubu2 ON ubu2.blocking_user_id = p.owner_id AND ubu2.blocked_user_id = :userId
            WHERE usp.user_id = :userId AND p.is_created = 1
            AND u.is_deactivated = 0
            AND ubu1.blocked_user_id IS NULL
            AND ubu2.blocking_user_id IS NULL
            ORDER BY p.date_time_created DESC
            """, nativeQuery = true)
    Slice<Post> findAllSavedByUserIdOrderByUploadDateDesc(long userId, Pageable pageable);


    @Query(value = """
            SELECT p.*
            FROM posts p
            JOIN users u ON p.owner_id = u.id
            JOIN users_have_tagged_posts uhtp ON p.id = uhtp.post_id
            LEFT JOIN users_block_users ubu1 ON ubu1.blocked_user_id = p.owner_id AND ubu1.blocking_user_id = :userId
            LEFT JOIN users_block_users ubu2 ON ubu2.blocking_user_id = p.owner_id AND ubu2.blocked_user_id = :userId
            WHERE uhtp.user_id = :ownerId AND p.is_created = 1
            AND u.is_deactivated = 0
            AND ubu1.blocked_user_id IS NULL
            AND ubu2.blocking_user_id IS NULL
            ORDER BY p.date_time_created DESC
            """, nativeQuery = true)
    Slice<Post> findAllOwnerTaggedOrderByUploadDateDesc(long userId, long ownerId, Pageable pageable);


    @Query(value = """
            SELECT p.*
            FROM posts p
            JOIN users u ON p.owner_id = u.id
            JOIN followers f ON u.id = f.followed_user_id
            LEFT JOIN users_block_users ubu1 ON ubu1.blocked_user_id = p.owner_id AND ubu1.blocking_user_id = :userId
            LEFT JOIN users_block_users ubu2 ON ubu2.blocking_user_id = p.owner_id AND ubu2.blocked_user_id = :userId
            WHERE f.following_user_id = :userId
            AND p.is_created = 1
            AND p.date_time_created > CURRENT_TIMESTAMP - INTERVAL 2 DAY
            AND u.is_deactivated = 0
            AND ubu1.blocked_user_id IS NULL
            AND ubu2.blocking_user_id IS NULL
            ORDER BY p.date_time_created DESC
            """, nativeQuery = true)
    Slice<Post> findAllByUserFollowersOrderByUploadDateDesc(long userId, Pageable pageable);
}
