package bg.ittalents.instagram.comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query(value = """
            SELECT 
                c.*, 
                COUNT(chl.user_id) AS numberOfLikes
            FROM comments c
            LEFT JOIN comments_have_likes chl 
            ON c.id = chl.comment_id
            WHERE c.parent_id = :parentId
            GROUP BY c.id
            ORDER BY numberOfLikes DESC
            """, nativeQuery = true)
    Slice<Comment> findRepliesByParentIdOrderByNumberOfLikes(long parentId, Pageable pageable);

    @Query(value = """
            SELECT 
                c.*,
                COUNT(chl.user_id) AS numberOfLikes
            FROM comments c
            LEFT JOIN comments_have_likes chl
            ON c.id = chl.comment_id
            WHERE c.parent_id IS NULL
            AND c.post_id = :postId
            GROUP BY c.id
            ORDER BY numberOfLikes DESC
            """,
            nativeQuery = true)
    Slice<Comment> findParentCommentsByPostIdOrderByNumberOfLikes(long postId, Pageable pageable);

    int countAllByPostId(long id);
}

