package com.app.controller;

import com.app.exception.ForbiddenException;
import com.app.exception.ResourceNotFoundException;
import com.app.model.*;
import com.app.projection.UserProjection;
import com.app.repo.MessageRepository;
import com.app.repo.ReportRepository;
import com.app.repo.UserRepository;
import com.app.service.ChatroomService;
import com.app.service.FileService;
import com.app.service.CurrentUserService;
import com.app.service.MessageService;
import com.app.session.UserSessionBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ChatroomController handles chatroom-related operations such as starting conversations,
 * creating communities, managing chatrooms, and viewing messages.
 */
@Controller
@RequestMapping("/chatrooms")
public class ChatroomController {

    /**
     * Service to handle chatroom operations.
     */
    @Autowired
    private ChatroomService chatroomService;

    /**
     * Repository to access user data.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Service to handle current user operations.
     */
    @Autowired
    private CurrentUserService currentUserService;

    /**
     * Repository to access message data.
     */
    @Autowired
    private MessageRepository messageRepository;

    /**
     * Service to handle message operations.
     */
    @Autowired
    private MessageService messageService;

    /**
     * Service to handle file operations.
     */
    @Autowired
    private FileService fileService;

    /**
     * Session bean to manage user session data.
     */
    @Autowired
    private UserSessionBean userSession;

    /**
     * Repository to access report data.
     */
    @Autowired
    private ReportRepository reportRepository;

    /**
     * Displays the page to start a new conversation with another user.
     *
     * @param query the search query for users (optional)
     * @param model the model to add attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("/conversations/start")
    public String showStartConversationPage(@RequestParam(value = "query", required = false) String query, Model model) {
        User currentUser = currentUserService.getCurrentAppUser();
        List<User> users = List.of();
        if (query != null && !query.trim().isEmpty()) {
            userRepository.searchNonAdminUsers(query);
        }
        model.addAttribute("query", query);
        model.addAttribute("users", users);
        return "start-conversation";
    }

    /**
     * Starts a conversation with another user by creating or finding a private chatroom.
     *
     * @param userId the ID of the user to start a conversation with
     * @return a redirect to the chatroom view
     */
    @PostMapping("/conversations/start/{userId}")
    public String startConversation(@PathVariable String userId) {
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow(() -> new ResourceNotFoundException("Current user not found."));
        if (user.getId().toString().equals(userId)) {
            return "redirect:/home";
        }
        User otherUser = userRepository.findById(Long.parseLong(userId)).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if ("ADMIN".equals(otherUser.getRole())) {
            throw new ForbiddenException("You cannot chat with admin.");
        }
        Chatroom chat = chatroomService.findOrCreatePrivateChat(user.getId(), Long.parseLong(userId));
        return "redirect:/chatrooms/" + chat.getId() + "/view-chatroom";
    }

    /**
     * Searches for members in a chatroom and displays the results.
     *
     * @param chatroomId the ID of the chatroom to search in
     * @param query      the search query for users (optional)
     * @param model      the model to add attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("/{chatroomId}/search-members")
    public String searchMembers(@PathVariable Long chatroomId,
                                @RequestParam(required = false) String query,
                                Model model) {
        User user = chatroomService.requireMembershipOrThrow(chatroomId);
        Chatroom chatroom = chatroomService.findById(chatroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chatroom not found with id: " + chatroomId));
        List<User> members = chatroomService.getChatroomMembers(chatroomId);
        List<UserProjection> users = (query == null || query.isEmpty())
                ? List.of()
                : chatroomService.searchUsersNotInGroup(chatroomId, query);
        model.addAttribute("chatroom", chatroom);
        model.addAttribute("members", members);
        model.addAttribute("users", users);
        model.addAttribute("chatroomId", chatroomId);
        model.addAttribute("query", query);
        model.addAttribute("chatroomType", chatroomService.findById(chatroomId)
                .map(Chatroom::getType)
                .map(Enum::toString)
                .orElse("UNKNOWN"));
        model.addAttribute("editNameMode", false);
        return "chatroom-manage";
    }

    /**
     * Displays the page to create a new community chatroom.
     *
     * @return the name of the view to render
     */
    @GetMapping("/create-community")
    public String createCommunity() {
        return "chatroom-create-community";
    }

    /**
     * Creates a new community chatroom with the specified name.
     *
     * @param name          the name of the community
     * @param editableName  whether the name is editable
     * @return a redirect to the newly created chatroom's view
     */
    @PostMapping("/create-community")
    public String createCommunity(@RequestParam String name,
                                  @RequestParam(required = false) boolean editableName) {
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUser.getAttribute("email")));
        Chatroom chatroom = chatroomService.createCommunity(name, user);
        return "redirect:/chatrooms/" + chatroom.getId() + "/view-chatroom";
    }

    /**
     * Displays the discoverable communities for the current user.
     *
     * @param model the model to add attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("/getCommunities")
    public String getCommunities(Model model) {
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUser.getAttribute("email")));
        List<Chatroom> communities = chatroomService.findDiscoverableCommunities(user.getId());
        model.addAttribute("communities", communities);
        return "discover";
    }

    /**
     * Searches for communities based on a query and displays the results.
     *
     * @param query the search query for communities
     * @param model the model to add attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("/search-community")
    public String searchCommunity(@RequestParam String query, Model model) {
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUser.getAttribute("email")));
        List<Chatroom> foundCommunities = chatroomService.searchCommunities(query, user.getId());
        model.addAttribute("foundCommunities", foundCommunities);
        model.addAttribute("query", query);
        return "discover";
    }

    /**
     * Displays the chatrooms that the current user is a member of.
     *
     * @param model the model to add attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("")
    public String myChatrooms(Model model) {
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUser.getAttribute("email")));
        List<Chatroom> myChats = chatroomService.findMyChatrooms(user.getId());
        Map<Long, String> displayNames = myChats.stream()
                .collect(Collectors.toMap(
                        Chatroom::getId,
                        chat -> chat.getDisplayName(user)
                ));
        model.addAttribute("chatrooms", myChats);
        model.addAttribute("displayNames", displayNames);
        return "chatrooms";
    }

    /**
     * Displays the discoverable communities for the current user.
     *
     * @param model the model to add attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("/discover")
    public String discoverCommunities(Model model) {
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUser.getAttribute("email")));
        List<Chatroom> communities = chatroomService.findDiscoverableCommunities(user.getId());
        model.addAttribute("communities", communities);
        return "discover";
    }

    /**
     * Joins a community chatroom.
     *
     * @param chatroomId the ID of the chatroom to join
     * @return a redirect to the discover communities page
     */
    @PostMapping("/join/{chatroomId}")
    public String joinCommunity(@PathVariable Long chatroomId) {
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUser.getAttribute("email")));
        chatroomService.joinCommunity(chatroomId, user.getId());
        return "redirect:/chatrooms/discover";
    }

    /**
     * Leaves a chatroom.
     *
     * @param chatroomId the ID of the chatroom to leave
     * @return a redirect to the chatrooms page
     */
    @PostMapping("/leave/{chatroomId}")
    public String leaveChatroom(@PathVariable Long chatroomId) {
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUser.getAttribute("email")));
        chatroomService.leaveChatroom(chatroomId, user.getId());
        return "redirect:/chatrooms";
    }

    /**
     * Deletes a chatroom.
     *
     * @param chatroomId the ID of the chatroom to delete
     * @return a redirect to the chatrooms page
     */
    @PostMapping("/{chatroomId}/edit")
    public String editChatroomName(@PathVariable Long chatroomId, @RequestParam String name, Model model) {
        User user = chatroomService.requireMembershipOrThrow(chatroomId);
        Chatroom chatroom = chatroomService.editChatroomName(chatroomId, user.getId(), name);
        model.addAttribute("chatroom", chatroom);
        return "redirect:/chatrooms/" + chatroomId + "/manage";
    }

    /**
     * Adds a user to a group chatroom.
     *
     * @param chatroomId the ID of the chatroom
     * @param userId     the ID of the user to add
     * @param model      the model to add attributes for the view
     * @return a redirect to the chatroom management page
     */
    @PostMapping("/{chatroomId}/add-member/{userId}")
    public String addUserToGroup(@PathVariable Long chatroomId, @PathVariable Long userId, Model model) {
        chatroomService.requireMembershipOrThrow(chatroomId);
        Chatroom chatroom = chatroomService.findById(chatroomId).orElseThrow(() -> new ResourceNotFoundException("Chatroom not found with id: " + chatroomId));
        chatroomService.addUserToGroup(chatroomId, userId);
        model.addAttribute("chatroom", chatroom);
        return "redirect:/chatrooms/" + chatroomId + "/manage";
    }

    /**
     * Displays the page to create a new group chatroom.
     *
     * @return the name of the view to render
     */
    @GetMapping("/create-group")
    public String createGroup() {
        return "chatroom-create-group";
    }

    /**
     * Displays the page to start a conversation with another user.
     *
     * @return the name of the view to render
     */
    @GetMapping("/create-conversation")
    public String createConversation() {
        return "start-conversation";
    }

    /**
     * Creates a new group chatroom with the specified name.
     *
     * @param name          the name of the group
     * @param editableName  whether the name is editable
     * @return a redirect to the newly created chatroom's view
     */
    @PostMapping("/create")
    public String createGroup(@RequestParam String name,
                              @RequestParam(required = false) boolean editableName) {
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUser.getAttribute("email")));
        Chatroom chatroom = chatroomService.createGroup(name, editableName, user);
        return "redirect:/chatrooms/" + chatroom.getId() + "/view-chatroom";
    }

    /**
     * Displays the chatroom view for a specific chatroom.
     *
     * @param chatroomId the ID of the chatroom to view
     * @param model      the model to add attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("/{chatroomId}/view-chatroom")
    public String viewChatroom(@PathVariable Long chatroomId, Model model) {
        userSession.visitChatroom(chatroomId);
        Chatroom chatroom = chatroomService.findById(chatroomId).orElseThrow(() -> new ResourceNotFoundException("Chatroom not found with id: " + chatroomId));
        List<Message> messages = messageRepository.findByChatroomOrderByTimestampAsc(chatroom);
        User user = chatroomService.requireMembershipOrThrow(chatroomId);
        model.addAttribute("chatroomId", chatroomId);
        model.addAttribute("chatroomType", chatroom.getType().toString());
        model.addAttribute("messages", messages);
        model.addAttribute("currentUserId", user.getId());
        model.addAttribute("currentUserName", user.getUsername());
        model.addAttribute("chatroomName", chatroom.getDisplayName(user));
        model.addAttribute("chatroomType", chatroom.getType().toString());
        List<Report> myReports = reportRepository.findAllByReporter(user);
        Set<Long> reportedByMe = myReports.stream().map(r -> r.getReportedMessage().getId()).collect(Collectors.toSet());
        model.addAttribute("reportedByMeIds", reportedByMe);
        return "view-chatroom";
    }

    /**
     * Displays the management page for a specific chatroom.
     *
     * @param chatroomId the ID of the chatroom to manage
     * @param query      the search query for users (optional)
     * @param editNameParam whether to enable edit name mode (default is false)
     * @param model      the model to add attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("/{chatroomId}/manage")
    public String manageChatroom(@PathVariable Long chatroomId,
                                 @RequestParam(required = false) String query,
                                 @RequestParam(name = "editName", defaultValue = "false") String editNameParam,
                                 Model model) {
        boolean editNameMode = "true".equalsIgnoreCase(editNameParam);
        chatroomService.requireMembershipOrThrow(chatroomId);
        Chatroom chatroom = chatroomService.findById(chatroomId).orElseThrow(() -> new ResourceNotFoundException("Chatroom not found with id: " + chatroomId));
        model.addAttribute("chatroomId", chatroomId);
        model.addAttribute("chatroomType", chatroom.getType().toString());
        List<User> members = chatroomService.getChatroomMembers(chatroomId);
        model.addAttribute("members", members);
        List<UserProjection> users = (query == null || query.isEmpty()) ? List.of() : chatroomService.searchUsersNotInGroup(chatroomId, query);
        model.addAttribute("query", query);
        model.addAttribute("users", users);
        model.addAttribute("chatroom", chatroom);
        model.addAttribute("editNameMode", editNameMode);
        return "chatroom-manage";
    }

    /**
     * Displays the confirmation page for leaving a chatroom.
     *
     * @param id    the ID of the chatroom to leave
     * @param model the model to add attributes for the view
     * @return the name of the view to render
     */
    @GetMapping("/{id}/leave-confirm")
    public String confirmLeave(@PathVariable Long id, Model model) {
        chatroomService.requireMembershipOrThrow(id);
        model.addAttribute("chatroomId", id);
        return "chatroom-leave-confirm";
    }
}
