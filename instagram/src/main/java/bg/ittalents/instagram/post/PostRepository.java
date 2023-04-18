package bg.ittalents.instagram.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = """
            SELECT
                *
            FROM posts
            WHERE owner_id = ?1
            ORDER BY date_time_created DESC
            """, nativeQuery = true)
    Page<Post> findByOwnerIdOrderByUploadDateDesc(Long ownerId, Pageable pageable);

    @Query(value = """
            SELECT 
                p.* 
            FROM posts p 
            JOIN posts_have_hashtags phh 
            ON p.id = phh.post_id 
            JOIN hashtags h 
            ON phh.hashtag_id = h.id 
            WHERE h.name = :hashtagName AND p.is_created = 1
            ORDER BY p.date_time_created DESC""",
            nativeQuery = true)
    Page<Post> findByHashtagNameSortedByDateTimeCreatedDesc(@Param("hashtagName") String hashtagName,
                                                            Pageable pageable);


    @Query(value = """
            SELECT 
                p.* 
            FROM posts p 
            JOIN locations l 
            ON p.location_id = l.id 
            WHERE l.name = :locationName AND p.is_created = 1
            ORDER BY p.date_time_created 
            DESC""",
            nativeQuery = true)
    Page<Post> findByLocationNameSortedByDateTimeCreatedDesc(@Param("locationName") String locationName,
                                                             Pageable pageable);

}
