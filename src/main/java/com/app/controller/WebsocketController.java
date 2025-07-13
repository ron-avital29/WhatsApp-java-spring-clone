package com.app.controller;

import com.app.dto.ChatMessageDTO;
import com.app.model.Chatroom;
import com.app.model.File;
import com.app.model.Message;
import com.app.model.User;
import com.app.repo.UserRepository;
import com.app.service.ChatroomService;
import com.app.service.CurrentUserService;
import com.app.service.FileService;
import com.app.service.MessageService;
import com.app.listeners.WebSocketEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.security.Principal;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Controller
public class WebsocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChatroomService chatroomService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private WebSocketEventListener webSocketEventListener;

    @MessageMapping("/chat")
    public void send(ChatMessageDTO message) {
        Chatroom chatroom = chatroomService.findById(message.getChatroomId())
                .orElseThrow(() -> new IllegalArgumentException("Chatroom not found"));

        User sender = userRepository.findById(message.getFromId())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

        File file = null;
        if (message.getFileId() != null) {
            file = fileService.getFileById(message.getFileId());
            if (file == null) {
                throw new IllegalArgumentException("File not found with ID: " + message.getFileId());
            }
        }

        Message savedMessage = messageService.sendMessageToChatroom(message.getText(), chatroom, sender, file);
        message.setId(savedMessage.getId());

        message.setFrom(sender.getUsername());
        message.setTime(new SimpleDateFormat("HH:mm").format(new Date()));
        message.setFromId(sender.getId());

        if (file != null) {
            message.setFilename(file.getFilename());
        }

        messagingTemplate.convertAndSend("/topic/messages/" + chatroom.getId(), message);
    }

    @MessageMapping("/join")
    public void handleJoin(Map<String, Long> payload, StompHeaderAccessor accessor) {
        Long chatroomId = payload.get("chatroomId");
        Principal user = accessor.getUser();
        String sessionId = accessor.getSessionId();

        if (user != null && chatroomId != null) {
            webSocketEventListener.addUserToChatroom(user.getName(), chatroomId, sessionId);
        }
    }
}
