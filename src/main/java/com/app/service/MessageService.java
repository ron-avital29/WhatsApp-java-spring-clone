package com.app.service;

import com.app.dto.ChatMessageDTO;
import com.app.exception.ResourceNotFoundException;
import com.app.model.Chatroom;
import com.app.model.File;
import com.app.model.Message;
import com.app.model.User;
import com.app.repo.MessageRepository;
import com.app.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatroomService chatroomService;

    @Autowired
    private FileService fileService;

    public Message sendMessageToChatroom(String content, Chatroom chatroom, User sender, File file) {
        Message message = new Message();
        message.setContent(content);
        message.setChatroom(chatroom);
        message.setSender(sender);
        message.setTimestamp(LocalDateTime.now());
        message.setFile(file);
        return messageRepository.save(message);
    }

    public ChatMessageDTO sendChatMessage(ChatMessageDTO dto) {
        Chatroom chatroom = chatroomService.findById(dto.getChatroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Chatroom not found"));

        User sender = userRepository.findById(dto.getFromId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        File file = null;
        if (dto.getFileId() != null) {
            file = fileService.getFileById(dto.getFileId());
            if (file == null) {
                throw new ResourceNotFoundException("File not found with ID: " + dto.getFileId());
            }
        }

        Message saved = sendMessageToChatroom(dto.getText(), chatroom, sender, file);

        dto.setId(saved.getId());
        dto.setFrom(sender.getUsername());
        dto.setTime(new SimpleDateFormat("HH:mm").format(new Date()));
        dto.setFromId(sender.getId());

        if (file != null) {
            dto.setFilename(file.getFilename());
        }

        return dto;
    }
}
