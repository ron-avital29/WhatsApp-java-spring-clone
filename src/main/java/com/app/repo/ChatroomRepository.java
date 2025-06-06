package com.app.repo;

import com.app.model.Chatroom;
import com.app.model.ChatroomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {

    // Optional helper methods:

    List<Chatroom> findByType(ChatroomType type);

    List<Chatroom> findByMembers_Id(Long userId); // "My Chats" feature
}
