package bg.ittalents.instagram.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = """
            SELECT u.*
            FROM users u
            LEFT JOIN users_block_users ubu ON u.id = ubu.blocking_user_id AND ubu.blocked_user_id = :loggedId
            WHERE u.id = :searchUserId
            AND u.is_deactivated = 0
            AND ubu.blocking_user_id IS NULL
            """, nativeQuery = true)
    Optional<User> findByIdNotBlocked(long loggedId, long searchUserId);


    @Query(value = """
            SELECT u.*
            FROM users u
            WHERE u.id = :userId
            AND u.is_deactivated = 0
            """, nativeQuery = true)
    Optional<User> findById(long userId);
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
            JOIN followers f ON u.id = f.following_user_id
            WHERE f.followed_user_id = :userId
            AND u.is_deactivated = 0
            """, nativeQuery = true)
    List<User> findAllFollowers(final long userId);

    @Query(value = """
            SELECT *
            FROM users u
            JOIN followers f ON u.id = f.followed_user_id
            WHERE f.following_user_id = :userId
            AND u.is_deactivated = 0
            ORDER BY f.date_time_of_follow DESC
            """, nativeQuery = true)
    Slice<User> findAllFollowedOrderByDateOfFollowDesc(long userId, Pageable pageable);

    @Query(value = """
            SELECT *
            FROM users u
            JOIN followers f ON u.id = f.followed_user_id
            WHERE f.following_user_id = :userId
            AND u.is_deactivated = 0
            """, nativeQuery = true)
    List<User> findAllFollowed(final long userId);


    @Query(value = """
            SELECT u.*
            FROM users u
            LEFT JOIN users_block_users bu2 ON bu2.blocking_user_id = u.id AND bu2.blocked_user_id = :userId
            WHERE (u.username LIKE CONCAT('%', :username, '%')
            OR u.name LIKE CONCAT('%', :username, '%')) 
            AND u.is_deactivated = 0 
            AND bu2.blocking_user_id IS NULL
            ORDER BY u.username ASC
            LIMIT 55
            """, nativeQuery = true)
    List<User> findAllUsersOrderById(String username, long userId);

    @Query(value = """
            SELECT COUNT(*)
            FROM users u
            JOIN followers f ON f.followed_user_id = :userId
            WHERE f.following_user_id = u.id
            AND u.is_deactivated = 0
            """, nativeQuery = true)
    int countFollowersByUserIdAndDeactivatedFalse(long userId);

    @Query(value = """
            SELECT COUNT(*)
            FROM users u
            JOIN followers f ON f.following_user_id = :userId
            WHERE f.followed_user_id = u.id
            AND u.is_deactivated = 0
            """, nativeQuery = true)
    int countFollowingByUserIdAndDeactivatedFalse(long userId);
}