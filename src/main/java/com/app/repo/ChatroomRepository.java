package com.app.repo;

import com.app.model.Chatroom;
import com.app.model.ChatroomType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.app.projection.UserProjection;

import java.util.List;
import java.util.Set;

@Repository
public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {

    List<Chatroom> findByMembers_Id(Long userId);

    @Query("SELECT c FROM Chatroom c WHERE c.type = 'COMMUNITY' AND :userId NOT IN (SELECT u.id FROM c.members u)")// for debug - show all communities
    List<Chatroom> findCommunitiesNotJoinedByUser(@Param("userId") Long userId);

    @Query("SELECT u.id AS id, u.username AS username, u.email AS email " +
            "FROM User u " +
            "WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<UserProjection> searchUsersByUsername(@Param("query") String query);

    @Query("SELECT u.id AS id, u.username AS username, u.email AS email " +
            "FROM User u " +
            "WHERE (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND u.role <> 'ADMIN' " +
            "AND u.id NOT IN (" +
            "   SELECT m.id FROM Chatroom c JOIN c.members m WHERE c.id = :chatroomId" +
            ")")
    List<UserProjection> searchUsersNotInGroup(@Param("chatroomId") Long chatroomId, @Param("query") String query);

    @Query("SELECT c FROM Chatroom c JOIN c.members m " +
            "WHERE m.id IN :memberIds " +
            "GROUP BY c.id " +
            "HAVING COUNT(m) = :memberCount AND c.type = :type")
    List<Chatroom> findPrivateChatByMembers(@Param("memberIds") Set<Long> memberIds,
                                            @Param("memberCount") long memberCount,
                                            @Param("type") ChatroomType type);

    @Query("SELECT c FROM Chatroom c WHERE c.type = 'COMMUNITY' " +
            "AND LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "AND :userId NOT IN (SELECT m.id FROM c.members m)")
    List<Chatroom> searchCommunities(String query, Long userId);
}

