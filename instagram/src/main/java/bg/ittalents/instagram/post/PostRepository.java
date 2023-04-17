package bg.ittalents.instagram.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    List<Post> findByOwnerIdOrderByUploadDateDesc(Long ownerId);
}
