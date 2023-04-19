package bg.ittalents.instagram.post;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = """
            SELECT
                *
            FROM posts
            WHERE id = ?1
            """, nativeQuery = true)
    Optional<Post> findByIdNotCreated(long id);

    @Query(value = """
            SELECT
                *
            FROM posts
            WHERE id = ?1 AND is_created = 1
            """, nativeQuery = true)
    Optional<Post> findById(long id);
    @Query(value = """
            SELECT
                *
            FROM posts
            WHERE owner_id = ?1 AND is_created = 1
            ORDER BY date_time_created DESC
            """, nativeQuery = true)
    Slice<Post> findByOwnerIdOrderByUploadDateDesc(long ownerId, Pageable pageable);

    @Query(value = """
            SELECT 
                p.* 
            FROM posts AS p 
            JOIN posts_have_hashtags phh 
            ON p.id = phh.post_id 
            JOIN hashtags h 
            ON phh.hashtag_id = h.id 
            WHERE h.name = :hashtagName AND p.is_created = 1
            ORDER BY p.date_time_created DESC""",
            nativeQuery = true)
    Slice<Post> findByHashtagNameSortedByDateTimeCreatedDesc(@Param("hashtagName") String hashtagName,
                                                             Pageable pageable);


    @Query(value = """
            SELECT 
                p.* 
            FROM posts AS p 
            JOIN locations AS l 
            ON p.location_id = l.id 
            WHERE l.name = :locationName AND p.is_created = 1
            ORDER BY p.date_time_created 
            DESC""",
            nativeQuery = true)
    Slice<Post> findByLocationNameSortedByDateTimeCreatedDesc(@Param("locationName") String locationName,
                                                             Pageable pageable);
    @Query(value = """
            SELECT
                *
            FROM posts AS p
            JOIN users_save_posts AS usp 
            ON p.id = usp.post_id
            WHERE usp.user_id = ?1 AND p.is_created = 1
            ORDER BY p.date_time_created DESC
            """, nativeQuery = true)
    Slice<Post> findTaggedByOwnerIdOrderByUploadDateDesc(long ownerId, Pageable pageable);
}
