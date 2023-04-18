package bg.ittalents.instagram.comment;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query(value = """
            SELECT c.*
            FROM comments c
            LEFT JOIN comments_have_likes chl ON c.id = chl.comment_id
            WHERE c.parent_id = ?1
            GROUP BY c.id
            ORDER BY COUNT(chl.user_id) DESC
            """, nativeQuery = true)
    Page<Comment> findByParentIdOrderByNumberOfLikes(long parentId, Pageable pageable);
}

