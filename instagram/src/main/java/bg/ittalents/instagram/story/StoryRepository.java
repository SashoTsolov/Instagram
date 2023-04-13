package bg.ittalents.instagram.story;

import bg.ittalents.instagram.post.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryRepository extends JpaRepository<Post, Long> {

    //TODO: Implement custom query methods for Story entity if necessary
}
