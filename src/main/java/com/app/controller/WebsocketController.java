package com.app.controller;

import com.app.dto.ChatMessageDTO;
import com.app.model.Chatroom;
import com.app.model.File;
import com.app.model.User;
import com.app.repo.UserRepository;
import com.app.service.ChatroomService;
import com.app.service.CurrentUserService;
import com.app.service.FileService;
import com.app.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class WebsocketController {

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

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessageDTO send(ChatMessageDTO message) {
        System.out.println("Received message: " + message.toString());

        Chatroom chatroom = chatroomService.findById(message.getChatroomId())
                .orElseThrow(() -> new IllegalArgumentException("Chatroom not found"));

        User sender = userRepository.findById(message.getFromId())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

//        // Optional: Validate the sender is a member of the chatroom
//        if (!chatroomService.isMember(chatroom.getId(), sender)) {
//            throw new AccessDeniedException("Sender is not a member of the chatroom");
//        }

        File file = null;
        if (message.getFileId() != null) {
            file = fileService.getFileById(message.getFileId());
            if (file == null) {
                throw new IllegalArgumentException("File not found with ID: " + message.getFileId());
            }
        }

        messageService.sendMessageToChatroom(message.getText(), chatroom, sender, file);

        message.setFrom(sender.getUsername());
        message.setTime(new SimpleDateFormat("HH:mm").format(new Date()));

        if (file != null) {
            message.setFilename(file.getFilename());
        }

        return message;
    }
}
