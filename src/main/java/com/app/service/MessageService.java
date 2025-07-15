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

/**
 * MessageService handles operations related to sending messages in chatrooms.
 * It interacts with repositories to save messages and retrieve necessary entities.
 */
@Service
public class MessageService {

    /**
     * Repositories for accessing message and user data.
     * These are injected by Spring's dependency injection mechanism.
     */
    @Autowired
    private MessageRepository messageRepository;

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Service for managing chatrooms.
     * It is used to retrieve chatroom details when sending messages.
     */
    @Autowired
    private ChatroomService chatroomService;

    /**
     * Service for handling file operations.
     * It is used to retrieve file details when sending messages with attachments.
     */
    @Autowired
    private FileService fileService;

    /**
     * Sends a message to a chatroom.
     * It creates a new Message object, sets its properties, and saves it to the repository.
     *
     * @param content  the content of the message
     * @param chatroom the chatroom where the message will be sent
     * @param sender   the user sending the message
     * @param file     an optional file attached to the message
     * @return the saved Message object
     */
    public Message sendMessageToChatroom(String content, Chatroom chatroom, User sender, File file) {
        Message message = new Message();
        message.setContent(content);
        message.setChatroom(chatroom);
        message.setSender(sender);
        message.setTimestamp(LocalDateTime.now());
        message.setFile(file);
        return messageRepository.save(message);
    }

    /**
     * Sends a chat message based on the provided ChatMessageDTO.
     * It retrieves the chatroom and sender user, checks for file attachment,
     * and saves the message to the repository.
     *
     * @param dto the ChatMessageDTO containing message details
     * @return the updated ChatMessageDTO with message ID and other details
     */
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
