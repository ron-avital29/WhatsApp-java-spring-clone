package com.app.controller;

import com.app.model.Chatroom;
import com.app.model.User;
import com.app.projection.UserProjection;
import com.app.repo.UserRepository;
import com.app.service.ChatroomService;
import com.app.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/chatrooms")
public class ChatroomController {

    @Autowired
    private ChatroomService chatroomService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @GetMapping("")
    public String myChatrooms(Model model) {
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow();

        List<Chatroom> myChats = chatroomService.findMyChatrooms(user.getId());
        model.addAttribute("chatrooms", myChats);

        return "chatrooms"; // create chatrooms.html page
    }

    @GetMapping("/discover")
    public String discoverCommunities(Model model) {
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow();

        List<Chatroom> communities = chatroomService.findDiscoverableCommunities(user.getId());
        model.addAttribute("communities", communities);

        return "discover";
    }

    @PostMapping("/join/{chatroomId}")
    public String joinCommunity(@PathVariable Long chatroomId) {
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow();

        chatroomService.joinCommunity(chatroomId, user.getId());
        return "redirect:/chatrooms/discover";
    }

    @PostMapping("/leave/{chatroomId}")
    public String leaveChatroom(@PathVariable Long chatroomId) {
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow();

        chatroomService.leaveChatroom(chatroomId, user.getId());
        return "redirect:/chatrooms";
    }

    @GetMapping("/{chatroomId}/search-members")
    public String viewChatroomMembers(@PathVariable Long chatroomId,
                                      @RequestParam(required = false) String query,
                                      Model model) {

        // maybe out this in a function to avoid code duplication
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow();
        if (!chatroomService.isUserMemberOfChatroom(chatroomId, user.getId())) {
            return "redirect:/chatrooms"; // or show an error page
        }

        List<User> members = chatroomService.getChatroomMembers(chatroomId);

        List<UserProjection> users = (query == null || query.isEmpty())
                ? List.of()
                : chatroomService.searchUsersNotInGroup(chatroomId, query);

        // prints the users found
        users.forEach(u -> System.out.println("User found: " + u.getUsername()));

        model.addAttribute("members", members);
        model.addAttribute("users", users);
        model.addAttribute("chatroomId", chatroomId);
        model.addAttribute("query", query);
        model.addAttribute("chatroomType", chatroomService.findById(chatroomId)
                .map(Chatroom::getType)
                .map(Enum::toString)
                .orElse("UNKNOWN"));

        return "chatroom-members";
    }


    @PostMapping("/{chatroomId}/edit")
    public String editChatroomName(@PathVariable Long chatroomId, @RequestParam String name) {
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow();

        chatroomService.editChatroomName(chatroomId, user.getId(), name);
        return "redirect:/chatrooms";
    }

    @PostMapping("/{chatroomId}/add-member/{userId}")
    public String addUserToGroup(@PathVariable Long chatroomId, @PathVariable Long userId) {
        chatroomService.addUserToGroup(chatroomId, userId);
        return "redirect:/chatrooms";   // placeholder, probably needs to change
    }

    @GetMapping("/create-group")
    public String createGroupForm() {
        return "chatroom-create-group";
    }

    @GetMapping("/create-conversation")
    public String createConversationForm() {
        return "start-conversation";
    }

    @PostMapping("/create")
    public String createGroup(@RequestParam String name,
                              @RequestParam(required = false) boolean editableName) {

        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow();

        chatroomService.createGroup(name, editableName, user);

        return "redirect:/chatrooms";
    }

    // might me depricated
    @GetMapping("/{chatroomId}/members")
    public String viewChatroomMembers(@PathVariable Long chatroomId, Model model) {
        // check if the logged-in user is a member of the chatroom and has permission to view members, if not, redirect or show error
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow();
        if (!chatroomService.isUserMemberOfChatroom(chatroomId, user.getId())) {
            return "redirect:/chatrooms"; // or show an error page
        }


        List<User> members = chatroomService.getChatroomMembers(chatroomId);
        model.addAttribute("members", members);
        model.addAttribute("chatroomId", chatroomId);


        // ADD THIS:
        Chatroom chatroom = chatroomService.findById(chatroomId).orElseThrow();
        model.addAttribute("chatroomType", chatroom.getType().toString());

        return "chatroom-members";
    }

    @GetMapping("/conversations/start")
    public String showStartConversationPage(@RequestParam(value = "query", required = false) String query, Model model) {
        User currentUser = currentUserService.getCurrentAppUser();

//        if (currentUser == null) {
//            return "redirect:/login"; // or error page
//        }

        List<User> users;
        if (query != null && !query.trim().isEmpty()) {
            users = userRepository.findByUsernameContainingIgnoreCase(query);
        }
        else {
            users = userRepository.findRandomUsersExcluding(currentUser.getId(), 10);
        }

        model.addAttribute("query", query);
        model.addAttribute("users", users);

        System.out.println("now im supposed to show the start conversation page");
        return "start-conversation";
    }

    @GetMapping("/{chatroomId}/manage")
    public String manageChatroom(@PathVariable Long chatroomId,
                                 @RequestParam(required = false) String query,
                                 Model model) {

        // Chatroom data
        Chatroom chatroom = chatroomService.findById(chatroomId).orElseThrow();
        model.addAttribute("chatroomId", chatroomId);
        model.addAttribute("chatroomType", chatroom.getType().toString());

        // Chatroom members
        List<User> members = chatroomService.getChatroomMembers(chatroomId);
        model.addAttribute("members", members);

        // Search users not in group
        List<UserProjection> users = (query == null || query.isEmpty())
                ? List.of()
                : chatroomService.searchUsersNotInGroup(chatroomId, query);
        model.addAttribute("query", query);
        model.addAttribute("users", users);

        return "chatroom-manage"; // This is the name of the new page (see next step)
    }

    @PostMapping("/conversations/start/{userId}")
    public String startConversation(@PathVariable Long userId,
                                    @AuthenticationPrincipal User currentUser) {

        System.out.println("now im supposed to show the start conversation page");
        if (currentUser.getId().equals(userId)) {
            return "redirect:/home"; // Cannot start a convo with self
        }

//        Chatroom chat = chatroomService.findOrCreatePrivateChat(currentUser.getId(), userId);
//        return "redirect:/chatrooms/" + chat.getId();
        return "redirect:/home";    // Placeholder for actual conversation start logic
    }
}
