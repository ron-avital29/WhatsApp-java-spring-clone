package com.app.service;

import com.app.exception.ForbiddenException;
import com.app.exception.ResourceNotFoundException;
import com.app.model.Chatroom;
import com.app.model.ChatroomType;
import com.app.model.User;
import com.app.projection.UserProjection;
import com.app.repo.ChatroomRepository;
import com.app.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * ChatroomService provides methods to manage chatrooms, including joining communities,
 * editing group names, leaving chatrooms, and creating new chatrooms.
 * It also handles user membership and community discovery.
 */
@Service
@Transactional
public class ChatroomService {

    /**
     * Repository to access chatroom data.
     * It provides methods to find, save, and manage chatrooms.
     */
    @Autowired
    private ChatroomRepository chatroomRepository;

    /**
     * Repository to access user data.
     * It provides methods to find users by ID and manage user-related operations.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Service to retrieve the current user's information.
     * It provides methods to get the currently logged-in user.
     */
    @Autowired
    private CurrentUserService currentUserService;

    /**
     * Finds all chatrooms that the user is a member of.
     * It retrieves chatrooms based on the user's ID.
     *
     * @param userId the ID of the user
     * @return a list of chatrooms that the user is a member of
     */
    public List<Chatroom> findMyChatrooms(Long userId) {
        return chatroomRepository.findByMembers_Id(userId);
    }

    /**
     * Finds all discoverable communities that the user has not joined.
     * It retrieves communities based on the user's ID.
     *
     * @param userId the ID of the user
     * @return a list of communities that the user can discover
     */
    public List<Chatroom> findDiscoverableCommunities(Long userId) {
        return chatroomRepository.findCommunitiesNotJoinedByUser(userId);
    }

    /**
     * Finds a chatroom by its ID.
     * It retrieves the chatroom if it exists, or returns an empty Optional if not found.
     *
     * @param chatroomId the ID of the chatroom
     * @return an Optional containing the chatroom if found, or empty if not
     */
    public Optional<Chatroom> findById(Long chatroomId) {
        return chatroomRepository.findById(chatroomId);
    }

    /**
     * Joins a community chatroom for the specified user.
     * It checks if the chatroom is of type COMMUNITY and adds the user to its members.
     *
     * @param chatroomId the ID of the chatroom to join
     * @param userId     the ID of the user joining the chatroom
     */
    public synchronized void joinCommunity(Long chatroomId, Long userId) {
        Chatroom chatroom = chatroomRepository.findById(chatroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chatroom not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (chatroom.getType() != ChatroomType.COMMUNITY) {
            throw new ForbiddenException("You can only join COMMUNITY chatrooms.");
        }

        if (!chatroom.getMembers().contains(user)) {
            chatroom.getMembers().add(user);
            chatroomRepository.save(chatroom);
        }
    }

    /**
     * Retrieves the members of a chatroom by its ID.
     * It returns a list of users who are members of the specified chatroom.
     *
     * @param chatroomId the ID of the chatroom
     * @return a list of users who are members of the chatroom
     */
    public List<User> getChatroomMembers(Long chatroomId) {
        Chatroom chatroom = chatroomRepository.findById(chatroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chatroom not found"));
        return List.copyOf(chatroom.getMembers());
    }

    /**
     * Edits the name of a group chatroom.
     * It checks if the chatroom is of type GROUP, if the user is a member,
     * and if the chatroom's name is editable before allowing the change.
     *
     * @param chatroomId the ID of the chatroom to edit
     * @param userId     the ID of the user requesting the edit
     * @param newName    the new name for the chatroom
     * @return the updated chatroom
     */
    public synchronized Chatroom editChatroomName(Long chatroomId, Long userId, String newName) {
        Chatroom chatroom = chatroomRepository.findById(chatroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chatroom not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (chatroom.getType() != ChatroomType.GROUP) {
            throw new ForbiddenException("Only GROUP chatroom names can be edited.");
        }

        if (!chatroom.isEditableName()) {
            throw new ForbiddenException("This group's name is not editable.");
        }

        if (!chatroom.getMembers().contains(user)) {
            throw new ForbiddenException("You must be a member to edit this group's name.");
        }

        chatroom.setName(newName);
        chatroomRepository.save(chatroom);
        return chatroom;
    }

    /**
     * Leaves a chatroom for the specified user.
     * It checks if the chatroom is of type PRIVATE and throws an exception if so,
     * otherwise removes the user from the chatroom's members.
     *
     * @param chatroomId the ID of the chatroom to leave
     * @param userId     the ID of the user leaving the chatroom
     */
    public synchronized void leaveChatroom(Long chatroomId, Long userId) {
        Chatroom chatroom = chatroomRepository.findById(chatroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chatroom not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (chatroom.getType() == ChatroomType.PRIVATE) {
            throw new ForbiddenException("Cannot leave private chatroom.");
        }

        chatroom.getMembers().remove(user);
        chatroomRepository.save(chatroom);
    }

    /**
     * Searches for users who are not in a specific group chatroom.
     * It returns a list of user projections based on the provided query.
     *
     * @param chatroomId the ID of the chatroom to search against
     * @param query      the search query for user names
     * @return a list of user projections matching the search criteria
     */
    public List<UserProjection> searchUsersNotInGroup(Long chatroomId, String query) {
        return chatroomRepository.searchUsersNotInGroup(chatroomId, query);
    }

    /**
     * Adds a user to a group chatroom.
     * It checks if the chatroom is of type GROUP, if the user is not an admin,
     * and if the user is not already a member before adding them.
     *
     * @param chatroomId the ID of the chatroom to add the user to
     * @param userId     the ID of the user to be added
     */
    @Transactional
    public synchronized void addUserToGroup(Long chatroomId, Long userId) {
        Chatroom chatroom = chatroomRepository.findById(chatroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chatroom not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (chatroom.getType() != ChatroomType.GROUP) {
            throw new ForbiddenException("Only GROUP chatrooms can add members.");
        }

        if ("ADMIN".equals(user.getRole())) {
            throw new ForbiddenException("You cannot add an admin to a group.");
        }

        if (!chatroom.getMembers().contains(user)) {
            chatroom.getMembers().add(user);
            chatroomRepository.save(chatroom);
        }
    }

    /**
     * Creates a new group chatroom with the specified name and editable name status.
     * It initializes the chatroom with the creator as the first member.
     *
     * @param name         the name of the group chatroom
     * @param editableName whether the group name can be edited later
     * @param creator      the user creating the group chatroom
     * @return the created chatroom
     */
    @Transactional
    public synchronized Chatroom createGroup(String name, boolean editableName, User creator) {
        Chatroom chatroom = new Chatroom();
        chatroom.setName(name);
        chatroom.setType(ChatroomType.GROUP);
        chatroom.setEditableName(editableName);
        chatroom.setCreatedBy(creator);
        chatroom.getMembers().add(creator);

        return chatroomRepository.save(chatroom);
    }

    /**
     * Checks if a user is a member of a specific chatroom.
     * It retrieves the chatroom by its ID and checks if the user ID is in the members list.
     *
     * @param chatroomId the ID of the chatroom
     * @param userId     the ID of the user to check
     * @return true if the user is a member, false otherwise
     */
    public boolean isUserMemberOfChatroom(Long chatroomId, Long userId) {
        Optional<Chatroom> chatroomOpt = chatroomRepository.findById(chatroomId);
        if (chatroomOpt.isEmpty()) {
            return false;
        }
        Chatroom chatroom = chatroomOpt.get();
        return chatroom.getMembers().stream()
                .anyMatch(member -> member.getId().equals(userId));
    }

    /**
     * Requires that the current user is a member of a specific chatroom.
     * If the user is not logged in or not a member, it throws a ForbiddenException.
     *
     * @param chatroomId the ID of the chatroom to check membership
     * @return the current user if they are a member
     * @throws ForbiddenException if the user is not logged in or not a member
     */
    public User requireMembershipOrThrow(Long chatroomId) {
        User user = currentUserService.getCurrentAppUser();
        if (user == null || !isUserMemberOfChatroom(chatroomId, user.getId())) {
            if (user == null) {
                throw new ForbiddenException("You must be logged in to access this chatroom.");
            }
            throw new ForbiddenException("You are not a member of this chatroom.");
        }
        return user;
    }

    /**
     * Creates a new community chatroom with the specified name and creator.
     * It initializes the chatroom as a COMMUNITY type and adds the creator as the first member.
     *
     * @param name the name of the community chatroom
     * @param user the user creating the community
     * @return the created community chatroom
     */
    public synchronized Chatroom createCommunity(String name, User user) {
        Chatroom chatroom = new Chatroom();
        chatroom.setName(name);
        chatroom.setType(ChatroomType.COMMUNITY);
        chatroom.setEditableName(false);
        chatroom.setCreatedBy(user);
        chatroom.getMembers().add(user);

        return chatroomRepository.save(chatroom);
    }

    /**
     * Finds or creates a private chatroom between two users.
     * If a private chatroom already exists between the two users, it returns that chatroom.
     * Otherwise, it creates a new private chatroom with both users as members.
     *
     * @param myId      the ID of the current user
     * @param otherId   the ID of the other user
     * @return the existing or newly created private chatroom
     */
    public synchronized Chatroom findOrCreatePrivateChat(Long myId, long otherId) {
        User myUser = userRepository.findById(myId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User otherUser = userRepository.findById(otherId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<Long> memberIds = Set.of(myId, otherId);
        List<Chatroom> existingChats = chatroomRepository.findPrivateChatByMembers(memberIds, memberIds.size(), ChatroomType.PRIVATE);

        if (!existingChats.isEmpty()) {
            return existingChats.get(0);
        }

        Chatroom chatroom = new Chatroom();
        chatroom.setName(myUser.getUsername() + " & " + otherUser.getUsername());
        chatroom.setType(ChatroomType.PRIVATE);
        chatroom.setEditableName(false);
        chatroom.setCreatedBy(myUser);
        chatroom.getMembers().add(myUser);
        chatroom.getMembers().add(otherUser);

        return chatroomRepository.save(chatroom);
    }

    /**
     * Searches for communities based on a query string.
     * It retrieves communities that match the query and are not already joined by the user.
     *
     * @param query  the search query
     * @param userId the ID of the user performing the search
     * @return a list of communities matching the search criteria
     */
    public List<Chatroom> searchCommunities(String query, Long userId) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return chatroomRepository.searchCommunities(query, userId);
    }
}
