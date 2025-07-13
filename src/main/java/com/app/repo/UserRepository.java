package com.app.repo;

import com.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) AND u.role <> 'ADMIN'")
    List<User> searchNonAdminUsers(@Param("query") String query);

    List<User> findByBannedUntilIsNotNullAndBannedUntilAfter(LocalDateTime now);

    @Query("SELECT u FROM User u WHERE u.bannedUntil IS NOT NULL AND u.bannedUntil < :now")
    List<User> findUsersWithExpiredBans(@Param("now") LocalDateTime now);
}

