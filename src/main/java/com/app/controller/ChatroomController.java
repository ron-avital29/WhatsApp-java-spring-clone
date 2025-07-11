package com.app.controller;

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
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/chatrooms")
public class ChatroomController {

    @Autowired
    private ChatroomService chatroomService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserSessionBean userSession;

    @Autowired
    private ReportRepository reportRepository;

    @PostMapping("/{chatroomId}/upload")
    @ResponseBody
    public ResponseEntity<?> uploadFile(
            @PathVariable Long chatroomId,
            @RequestParam("file") MultipartFile multipartFile) {

        System.out.println("DEBUG: Upload requested for chatroomId = " + chatroomId);

        // Ensure user is member of chatroom
        User user = chatroomService.requireMembershipOrThrow(chatroomId);

        try {
            File file = fileService.saveFile(multipartFile);
            return ResponseEntity.ok().body(Map.of(
                    "fileId", file.getId(),
                    "filename", file.getFilename()
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed: " + e.getMessage());
        }
    }


    @GetMapping("/files/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        System.out.println("DEBUG: Download requested for file id = " + id);

        File file = fileService.getFileById(id);

        if (file == null) {
            System.out.println("DEBUG: No file found for id = " + id);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                    .body("File wasn't available on site (not found)".getBytes(StandardCharsets.UTF_8));
        }

        System.out.println("DEBUG: File found: " + file.getFilename());

        if (file.getFileData() == null) {
            System.out.println("DEBUG: fileData is NULL for id = " + id);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                    .body("File wasn't available on site (no data)".getBytes(StandardCharsets.UTF_8));
        }

        System.out.println("DEBUG: File size = " + file.getFileData().length + " bytes");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(file.getFilename(), StandardCharsets.UTF_8)
                .build());

        return new ResponseEntity<>(file.getFileData(), headers, HttpStatus.OK);
    }


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

    @PostMapping("/conversations/start/{userId}")
    public String startConversation(@PathVariable String userId) {
        System.out.println("starting conversation with userId: " + userId);
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow();

        if (user.getId().toString().equals(userId)) {
            System.out.println("error, cannot start conversation with self");
            return "redirect:/home"; // Cannot start a convo with self
        }

        User otherUser = userRepository.findById(Long.parseLong(userId)).orElseThrow();

        if ("ADMIN".equals(otherUser.getRole())) {
            System.out.println("error, cannot start conversation with admin");
            return "redirect:/home"; // or show error page
        }

        Chatroom chat = chatroomService.findOrCreatePrivateChat(user.getId(), Long.parseLong(userId));
        return "redirect:/chatrooms/" + chat.getId() + "/view-chatroom";
    }

    @GetMapping("/{chatroomId}/search-members")
    public String searchMembers(@PathVariable Long chatroomId,
                                @RequestParam(required = false) String query,
                                Model model) {

        User user = chatroomService.requireMembershipOrThrow(chatroomId);
        Chatroom chatroom = chatroomService.findById(chatroomId).orElseThrow();
        List<User> members = chatroomService.getChatroomMembers(chatroomId);
        List<UserProjection> users = (query == null || query.isEmpty())
                ? List.of()
                : chatroomService.searchUsersNotInGroup(chatroomId, query);

        // prints the users found
        users.forEach(u -> System.out.println("User found: " + u.getUsername()));

        model.addAttribute("chatroom", chatroom);
        model.addAttribute("members", members);
        model.addAttribute("users", users);
        model.addAttribute("chatroomId", chatroomId);
        model.addAttribute("query", query);
        model.addAttribute("chatroomType", chatroomService.findById(chatroomId)
                .map(Chatroom::getType)
                .map(Enum::toString)
                .orElse("UNKNOWN"));
        System.out.println("now im supposed to show the chatroom manage page");
        model.addAttribute("editNameMode", false);
        return "chatroom-manage";
    }

    @GetMapping("/create-community")
    public String createCommunity() {
        return "chatroom-create-community";
    }

    @PostMapping("/create-community")
    public String createCommunity(@RequestParam String name,
                                  @RequestParam(required = false) boolean editableName) {
        System.out.println("creating community with name: " + name);
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow();

        Chatroom chatroom = chatroomService.createCommunity(name, user);
        return "redirect:/chatrooms/" + chatroom.getId() + "/view-chatroom";
    }

    @GetMapping("/getCommunities")
    public String getCommunities(Model model) {
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow();

        List<Chatroom> communities = chatroomService.findDiscoverableCommunities(user.getId());
        model.addAttribute("communities", communities);

        return "discover";
    }

    @GetMapping("/search-community")
    public String searchCommunity(@RequestParam String query, Model model) {
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow();

        List<Chatroom> foundCommunities = chatroomService.searchCommunities(query, user.getId());
        model.addAttribute("foundCommunities", foundCommunities);
        model.addAttribute("query", query);

        return "discover";
    }

    //---------------------------------------------------------
    @GetMapping("")
    public String myChatrooms(Model model) {
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow();

        List<Chatroom> myChats = chatroomService.findMyChatrooms(user.getId());
        model.addAttribute("chatrooms", myChats);

        return "chatrooms";
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

    @PostMapping("/{chatroomId}/edit")
    public String editChatroomName(@PathVariable Long chatroomId, @RequestParam String name, Model model) {
        User user = chatroomService.requireMembershipOrThrow(chatroomId);

        Chatroom chatroom = chatroomService.editChatroomName(chatroomId, user.getId(), name);
        model.addAttribute("chatroom", chatroom);
        return "redirect:/chatrooms/" + chatroomId + "/manage";
    }

    @PostMapping("/{chatroomId}/add-member/{userId}")
    public String addUserToGroup(@PathVariable Long chatroomId, @PathVariable Long userId, Model model) {
        chatroomService.requireMembershipOrThrow(chatroomId);
        Chatroom chatroom = chatroomService.findById(chatroomId).orElseThrow();
        chatroomService.addUserToGroup(chatroomId, userId);
        model.addAttribute("chatroom", chatroom);
        return "redirect:/chatrooms/" + chatroomId + "/manage";
    }

    @GetMapping("/create-group")
    public String createGroup() {
        return "chatroom-create-group";
    }

    @GetMapping("/create-conversation")
    public String createConversation() {
        return "start-conversation";
    }

    // this needs to be changed to be called "create-group"
    @PostMapping("/create")
    public String createGroup(@RequestParam String name,
                              @RequestParam(required = false) boolean editableName) {
        OAuth2User currentUser = currentUserService.getCurrentUser();
        User user = userRepository.findByEmail(currentUser.getAttribute("email")).orElseThrow();

        Chatroom chatroom = chatroomService.createGroup(name, editableName, user);

        return "redirect:/chatrooms/" + chatroom.getId() + "/view-chatroom";
    }

    @GetMapping("/{chatroomId}/view-chatroom")
    public String viewChatroom(@PathVariable Long chatroomId, Model model) {
        userSession.visitChatroom(chatroomId);
        Chatroom chatroom = chatroomService.findById(chatroomId).orElseThrow();

        List<Message> messages = messageRepository.findByChatroomOrderByTimestampAsc(chatroom);

        User user = chatroomService.requireMembershipOrThrow(chatroomId);

        model.addAttribute("chatroomId", chatroomId);
        model.addAttribute("chatroomType", chatroom.getType().toString());
        model.addAttribute("messages", messages);

        // I think this is not needed, but leaving it here for now; we can access userId in the backend
        model.addAttribute("currentUserId", user.getId());
        model.addAttribute("currentUserName", user.getUsername());

        List<Report> myReports = reportRepository.findAllByReporter(user); // not all users
        Set<Long> reportedByMe = myReports.stream()
                .map(r -> r.getReportedMessage().getId())
                .collect(Collectors.toSet());

        model.addAttribute("reportedByMeIds", reportedByMe); // rename for clarity


        // for debug - print the role of the user:
        System.out.println("DEBUG: User " + user.getUsername() + " has roles: " + user.getRole());

        return "view-chatroom";
    }

    @GetMapping("/{chatroomId}/manage")
    public String manageChatroom(@PathVariable Long chatroomId,
                                 @RequestParam(required = false) String query,
                                 @RequestParam(name = "editName", defaultValue = "false") String editNameParam,
                                 Model model) {

        boolean editNameMode = "true".equalsIgnoreCase(editNameParam);

        chatroomService.requireMembershipOrThrow(chatroomId);
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

        // Chatroom object + editNameMode flag
        model.addAttribute("chatroom", chatroom);
        model.addAttribute("editNameMode", editNameMode);
        System.out.println("DEBUG: editNameParam = " + editNameParam);
        return "chatroom-manage";
    }
}