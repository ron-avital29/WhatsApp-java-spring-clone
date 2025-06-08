package com.app.service;

import com.app.model.Chatroom;
import com.app.model.ChatroomType;
import com.app.model.User;
import com.app.repo.ChatroomRepository;
import com.app.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import com.app.projection.UserProjection;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatroomService {

    @Autowired
    private ChatroomRepository chatroomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrentUserService currentUserService;

    public List<Chatroom> findMyChatrooms(Long userId) {
        return chatroomRepository.findByMembers_Id(userId);
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

        if (chatroom.getType() != ChatroomType.COMMUNITY) {
            throw new IllegalStateException("You can only join COMMUNITY chatrooms.");
        }

        if (!chatroom.getMembers().contains(user)) {
            chatroom.getMembers().add(user);
            chatroomRepository.save(chatroom);
        }
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

    public List<UserProjection> searchUsersNotInGroup(Long chatroomId, String query) {
        return chatroomRepository.searchUsersNotInGroup(chatroomId, query);
    }

    @Transactional
    public void addUserToGroup(Long chatroomId, Long userId) {
        Chatroom chatroom = chatroomRepository.findById(chatroomId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        if (chatroom.getType() != ChatroomType.GROUP) {
            throw new IllegalStateException("Only GROUP chatrooms can add members.");
        }

        if (!chatroom.getMembers().contains(user)) {
            chatroom.getMembers().add(user);
            chatroomRepository.save(chatroom);
        }
    }

    @Transactional
    public void createGroup(String name, boolean editableName, User creator) {
        Chatroom chatroom = new Chatroom();
        chatroom.setName(name);
        chatroom.setType(ChatroomType.GROUP);
        chatroom.setEditableName(editableName);
        chatroom.setCreatedBy(creator);
        chatroom.getMembers().add(creator);

        chatroomRepository.save(chatroom);
    }

    public boolean isUserMemberOfChatroom(Long chatroomId, Long useId) {
        Optional<Chatroom> chatroomOpt = chatroomRepository.findById(chatroomId);
        if (chatroomOpt.isEmpty()) {
            return false;
        }
        Chatroom chatroom = chatroomOpt.get();
        return chatroom.getMembers().stream()
                .anyMatch(member -> member.getId().equals(useId));
    }

    public User requireMembershipOrThrow(Long chatroomId) {
        User user = currentUserService.getCurrentAppUser();
        if (user == null || !isUserMemberOfChatroom(chatroomId, user.getId())) {
            throw new AccessDeniedException("You are not a member of this chatroom.");
        }
        return user;
    }

}