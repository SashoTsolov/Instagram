package bg.ittalents.instagram.hashtag;

import bg.ittalents.instagram.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {


    Optional<Hashtag> findByName(String name);

    boolean existsByName(String name);

    @Query("""
            SELECT 
                p 
            FROM posts p 
            JOIN p.hashtags h 
            WHERE h.name = :hashtagName 
            ORDER BY p.dateTimeCreated DESC
            """)
    Page<Post> findPostsByHashtagNameSortedByDateTimeCreatedDesc(@Param("hashtagName") String hashtagName,
                                                                 Pageable pageable);

    List<Hashtag> findAll();
}