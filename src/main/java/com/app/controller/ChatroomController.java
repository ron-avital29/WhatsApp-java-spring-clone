package com.app.controller;

import com.app.model.Chatroom;
import com.app.model.User;
import com.app.projection.UserProjection;
import com.app.repo.UserRepository;
import com.app.service.ChatroomService;
import com.app.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
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

//    @GetMapping("/{chatroomId}/members")
//    public String viewChatroomMembers(@PathVariable Long chatroomId,
//                                      @RequestParam(required = false) String query,
//                                      Model model) {
//
//        List<User> members = chatroomService.getChatroomMembers(chatroomId);
//
//        List<UserProjection> users = (query == null || query.isEmpty())
//                ? List.of()
//                : chatroomService.searchUsersNotInGroup(chatroomId, query);
//
//        model.addAttribute("members", members);
//        model.addAttribute("users", users);
//        model.addAttribute("chatroomId", chatroomId);
//        model.addAttribute("query", query);
//
//        return "chatroom-members";
//    }


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
        return "redirect:/chatrooms/{chatroomId}/add-members";
    }

    @GetMapping("/create")
    public String createGroupForm() {
        return "chatroom-create";
    }

    @PostMapping("/create")
    public String createGroup(@RequestParam String name,
                              @RequestParam(required = false) boolean editableName) {

        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow();

        chatroomService.createGroup(name, editableName, user);

        return "redirect:/chatrooms";
    }

    @GetMapping("/{chatroomId}/members")
    public String viewChatroomMembers(@PathVariable Long chatroomId, Model model) {
        List<User> members = chatroomService.getChatroomMembers(chatroomId);
        model.addAttribute("members", members);
        model.addAttribute("chatroomId", chatroomId);

        // ADD THIS:
        Chatroom chatroom = chatroomService.findById(chatroomId).orElseThrow();
        model.addAttribute("chatroomType", chatroom.getType().toString());

        return "chatroom-members";
    }
}
