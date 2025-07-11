package com.app.repo;

import com.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByUsername(String username); // Not unique anymore

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByGoogleId(String googleId);

    List<User> findByUsernameContainingIgnoreCase(String query);

    @Query(value = "SELECT * FROM users WHERE id <> :currentUserId ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<User> findRandomUsersExcluding(@Param("currentUserId") Long currentUserId, @Param("limit") int limit);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) AND u.role <> 'ADMIN'")
    List<User> searchNonAdminUsers(@Param("query") String query);
}

