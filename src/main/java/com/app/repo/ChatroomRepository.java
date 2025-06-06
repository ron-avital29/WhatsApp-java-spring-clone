package com.app.repo;

import com.app.model.Chatroom;
import com.app.model.ChatroomType;
import com.app.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {

    List<Chatroom> findByType(ChatroomType type);

    List<Chatroom> findByMembers_Id(Long userId);

    @Query("SELECT c FROM Chatroom c WHERE c.type = 'COMMUNITY' AND :userId NOT IN (SELECT u.id FROM c.members u)")
    List<Chatroom> findCommunitiesNotJoinedByUser(@Param("userId") Long userId);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "AND u.id NOT IN (SELECT m.id FROM Chatroom c2 JOIN c2.members m WHERE c2.id = :chatroomId)")
    List<User> searchUsersNotInGroup(@Param("chatroomId") Long chatroomId, @Param("query") String query);

}
