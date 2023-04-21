package bg.ittalents.instagram.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByResetIdentifier(String identifier);

    boolean existsByName(String username);

    Optional<User> findByVerificationCode(String verificationToken);

    @Query(value = """
            SELECT *
            FROM users u
            JOIN followers f ON u.id = f.following_user_id
            WHERE f.followed_user_id = :userId
            AND u.is_deactivated = 0
            ORDER BY f.date_time_of_follow DESC
            """, nativeQuery = true)
    Slice<User> findAllFollowersOrderByDateOfFollowDesc(long userId, Pageable pageable);

    @Query(value = """
            SELECT *
            FROM users u
            JOIN followers f ON u.id = f.followed_user_id
            WHERE f.following_user_id = :userId
            AND u.is_deactivated = 0
            ORDER BY f.date_time_of_follow DESC
            """, nativeQuery = true)
    Slice<User> findAllFollowedOrderByDateOfFollowDesc(long userId, Pageable pageable);
}