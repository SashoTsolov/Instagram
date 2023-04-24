package bg.ittalents.instagram.follower;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    void deleteById(FollowKey followKey);
}
