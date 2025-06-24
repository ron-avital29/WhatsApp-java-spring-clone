package com.app.repo;

import com.app.model.Message;
import com.app.model.Chatroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByChatroomOrderByTimestampAsc(Chatroom chatroom);

    @Query("SELECT m FROM Message m WHERE SIZE(m.reports) > 0")
    List<Message> findReportedMessages();
}

