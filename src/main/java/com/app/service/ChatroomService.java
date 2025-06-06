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

    public List<Chatroom> findDiscoverableCommunities(Long userId) {
        return chatroomRepository.findCommunitiesNotJoinedByUser(userId);
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

    public List<User> getChatroomMembers(Long chatroomId) {
        Chatroom chatroom = chatroomRepository.findById(chatroomId).orElseThrow();
        return List.copyOf(chatroom.getMembers());
    }

    public void editChatroomName(Long chatroomId, Long userId, String newName) {
        Chatroom chatroom = chatroomRepository.findById(chatroomId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        if (chatroom.getType() != ChatroomType.GROUP) {
            throw new IllegalStateException("Only GROUP chatroom names can be edited.");
        }

        if (!chatroom.isEditableName()) {
            throw new IllegalStateException("This group's name is not editable.");
        }

        if (!chatroom.getMembers().contains(user)) {
            throw new IllegalStateException("You must be a member to edit this group's name.");
        }

        chatroom.setName(newName);
        chatroomRepository.save(chatroom);
    }


    public void leaveChatroom(Long chatroomId, Long userId) {
        Chatroom chatroom = chatroomRepository.findById(chatroomId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        if (chatroom.getType() == ChatroomType.PRIVATE) {
            throw new IllegalStateException("Cannot leave private chatroom.");
        }

        chatroom.getMembers().remove(user);
        chatroomRepository.save(chatroom);
    }

    public List<User> searchUsersToAddToGroup(Long chatroomId, String query) {
        Chatroom chatroom = chatroomRepository.findById(chatroomId).orElseThrow();

        if (chatroom.getType() != ChatroomType.GROUP) {
            throw new IllegalStateException("Only GROUP chatrooms can add members.");
        }

        return chatroomRepository.searchUsersNotInGroup(chatroomId, query);
    }

    public void addUserToGroup(Long chatroomId, Long userId) {
        Chatroom chatroom = chatroomRepository.findById(chatroomId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        if (chatroom.getType() != ChatroomType.GROUP) {
            throw new IllegalStateException("Only GROUP chatrooms can add members.");
        }

        chatroom.getMembers().add(user);
        chatroomRepository.save(chatroom);
    }


}
