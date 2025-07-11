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
import java.util.Set;

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

    public Chatroom editChatroomName(Long chatroomId, Long userId, String newName) {
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
        return chatroom;
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

        if ("ADMIN".equals(user.getRole())) {
            throw new AccessDeniedException("You cannot add an admin to a group.");
        }

        if (!chatroom.getMembers().contains(user)) {
            chatroom.getMembers().add(user);
            chatroomRepository.save(chatroom);
        }
    }

    @Transactional
    public Chatroom createGroup(String name, boolean editableName, User creator) {
        Chatroom chatroom = new Chatroom();
        chatroom.setName(name);
        chatroom.setType(ChatroomType.GROUP);
        chatroom.setEditableName(editableName);
        chatroom.setCreatedBy(creator);
        chatroom.getMembers().add(creator);

        Chatroom savedChatroom = chatroomRepository.save(chatroom);
        return savedChatroom;
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
            if (user == null) {
                throw new AccessDeniedException("You must be logged in to access this chatroom.");
            }
            throw new AccessDeniedException("You are not a member of this chatroom.");
        }
        return user;
    }

    public Chatroom createCommunity(String name, User user) {
        Chatroom chatroom = new Chatroom();
        chatroom.setName(name);
        chatroom.setType(ChatroomType.COMMUNITY);
        chatroom.setEditableName(false);
        chatroom.setCreatedBy(user);
        chatroom.getMembers().add(user);

        return chatroomRepository.save(chatroom);
    }

    public Chatroom findOrCreatePrivateChat(Long myId, long otherId) {
        User myUser = userRepository.findById(myId).orElseThrow();
        User otherUser = userRepository.findById(otherId).orElseThrow();

        Set<Long> memberIds = Set.of(myId, otherId);
        List<Chatroom> existingChats = chatroomRepository.findPrivateChatByMembers(memberIds, memberIds.size(), ChatroomType.PRIVATE);

        if (!existingChats.isEmpty()) {
            System.out.println("Found existing private chatroom for users: " + myUser.getUsername() + " and " + otherUser.getUsername());
            return existingChats.get(0);
        }
        System.out.println("Creating new private chatroom for users: " + myUser.getUsername() + " and " + otherUser.getUsername());

        Chatroom chatroom = new Chatroom();
        chatroom.setName(myUser.getUsername() + " & " + otherUser.getUsername());
        chatroom.setType(ChatroomType.PRIVATE);
        chatroom.setEditableName(false);
        chatroom.setCreatedBy(myUser);
        chatroom.getMembers().add(myUser);
        chatroom.getMembers().add(otherUser);

        return chatroomRepository.save(chatroom);
    }

    public List<Chatroom> searchCommunities(String query, Long userId) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return chatroomRepository.searchCommunities(query, userId);
    }

}