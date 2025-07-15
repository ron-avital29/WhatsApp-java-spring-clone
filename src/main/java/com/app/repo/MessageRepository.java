package com.app.repo;

import com.app.model.Message;
import com.app.model.Chatroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * MessageRepository provides methods to access and manipulate message data in the database.
 * It extends JpaRepository to leverage built-in CRUD operations and custom query methods.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByChatroomOrderByTimestampAsc(Chatroom chatroom);

    @Query("""
    SELECT m
    FROM Message m
    WHERE LOWER(m.content) LIKE LOWER(CONCAT('%', :query, '%'))
    ORDER BY m.timestamp DESC
    """)
    List<Message> searchMessagesInUsersChats(@Param("query") String query);
}
