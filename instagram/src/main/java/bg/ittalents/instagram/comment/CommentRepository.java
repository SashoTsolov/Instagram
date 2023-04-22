package bg.ittalents.instagram.comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query(value = """
        SELECT 
            c.*
        FROM comments c
        JOIN users u ON c.owner_id = u.id
        LEFT JOIN users_block_users ubu1 ON ubu1.blocked_user_id = c.owner_id AND ubu1.blocking_user_id = :userId
        LEFT JOIN users_block_users ubu2 ON ubu2.blocking_user_id = c.owner_id AND ubu2.blocked_user_id = :userId
        WHERE c.id = :commentId
        AND u.is_deactivated = 0
        AND ubu1.blocked_user_id IS NULL
        AND ubu2.blocking_user_id IS NULL
        """, nativeQuery = true)
    Optional<Comment> findCommentById(long userId, long commentId);
    @Query(value = """
        SELECT 
            c.*, 
            COUNT(chl.user_id) AS numberOfLikes
        FROM comments c
        LEFT JOIN comments_have_likes chl ON c.id = chl.comment_id
        JOIN users u ON c.owner_id = u.id
        LEFT JOIN users_block_users ubu1 ON ubu1.blocked_user_id = c.owner_id AND ubu1.blocking_user_id = :userId
        LEFT JOIN users_block_users ubu2 ON ubu2.blocking_user_id = c.owner_id AND ubu2.blocked_user_id = :userId
        WHERE c.parent_id = :parentId AND u.is_deactivated = 0
        AND ubu1.blocked_user_id IS NULL
        AND ubu2.blocking_user_id IS NULL
        GROUP BY c.id
        ORDER BY numberOfLikes DESC
        """, nativeQuery = true)
    Slice<Comment> findAllRepliesByParentIdOrderByNumberOfLikes(long userId, long parentId, Pageable pageable);

    @Query(value = """
        SELECT 
            c.*,
            COUNT(chl.user_id) AS numberOfLikes
        FROM comments c
        LEFT JOIN comments_have_likes chl ON c.id = chl.comment_id
        JOIN users u ON c.owner_id = u.id
        LEFT JOIN users_block_users ubu1 ON ubu1.blocked_user_id = c.owner_id AND ubu1.blocking_user_id = :userId
        LEFT JOIN users_block_users ubu2 ON ubu2.blocking_user_id = c.owner_id AND ubu2.blocked_user_id = :userId
        WHERE c.parent_id IS NULL AND c.post_id = :postId AND u.is_deactivated = 0
        AND ubu1.blocked_user_id IS NULL
        AND ubu2.blocking_user_id IS NULL
        GROUP BY c.id
        ORDER BY numberOfLikes DESC
        """, nativeQuery = true)
    Slice<Comment> findAllParentCommentsByPostIdOrderByNumberOfLikes(long userId, long postId, Pageable pageable);

    @Query(value = """
        SELECT 
            COUNT(*)
        FROM comments c
        JOIN users u ON c.owner_id = u.id
        LEFT JOIN users_block_users ubu1 ON ubu1.blocked_user_id = c.owner_id AND ubu1.blocking_user_id = :userId
        LEFT JOIN users_block_users ubu2 ON ubu2.blocking_user_id = c.owner_id AND ubu2.blocked_user_id = :userId
        WHERE c.post_id = :postId AND u.is_deactivated = 0
        AND ubu1.blocked_user_id IS NULL
        AND ubu2.blocking_user_id IS NULL
        """, nativeQuery = true)
    int countAllByPostId(long userId, long postId);
}

