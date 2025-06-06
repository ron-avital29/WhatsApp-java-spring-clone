package com.app.service;

import com.app.model.Chatroom;
import com.app.model.ChatroomType;
import com.app.model.User;
import com.app.repo.ChatroomRepository;
import com.app.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatroomService {

    @Autowired
    private ChatroomRepository chatroomRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Chatroom> findMyChatrooms(Long userId) {
        return chatroomRepository.findByMembers_Id(userId);
    }

    public List<Chatroom> findAllCommunities() {
        return chatroomRepository.findByType(ChatroomType.COMMUNITY);
    }

    public Optional<Chatroom> findById(Long chatroomId) {
        return chatroomRepository.findById(chatroomId);
    }

    public void joinCommunity(Long chatroomId, Long userId) {
        Chatroom chatroom = chatroomRepository.findById(chatroomId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        chatroom.getMembers().add(user);
        chatroomRepository.save(chatroom);
    }

    public void leaveChatroom(Long chatroomId, Long userId) {
        Chatroom chatroom = chatroomRepository.findById(chatroomId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        chatroom.getMembers().remove(user);
        chatroomRepository.save(chatroom);
    }
}
