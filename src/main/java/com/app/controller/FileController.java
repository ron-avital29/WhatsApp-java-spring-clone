package com.app.controller;

import com.app.model.File;
import com.app.model.User;
import com.app.service.ChatroomService;
import com.app.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Controller for handling file uploads and downloads in chatrooms.
 * Provides endpoints to upload files to a specific chatroom and download files by ID.
 */
@Controller
@RequestMapping("/files")
public class FileController {

    /**
     * Service to handle file operations such as saving and retrieving files.
     */
    @Autowired
    private FileService fileService;

    /**
     * Service to manage chatroom memberships and operations.
     */
    @Autowired
    private ChatroomService chatroomService;

    /**
     * Uploads a file to a specific chatroom.
     *
     * @param chatroomId the ID of the chatroom where the file will be uploaded
     * @param multipartFile the file to be uploaded
     * @return a response entity containing the file ID and filename if successful, or an error message if failed
     */
    @PostMapping("/{chatroomId}/upload")
    @ResponseBody
    public ResponseEntity<?> uploadFile(@PathVariable Long chatroomId, @RequestParam("file") MultipartFile multipartFile) {
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

    /**
     * Downloads a file by its ID.
     *
     * @param id the ID of the file to be downloaded
     * @return a response entity containing the file data and headers if successful, or an error message if failed
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        File file = fileService.getFileById(id);

        if (file == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                    .body("File wasn't available on site (not found)".getBytes(StandardCharsets.UTF_8));
        }

        if (file.getFileData() == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8")
                    .body("File wasn't available on site (no data)".getBytes(StandardCharsets.UTF_8));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(file.getFilename(), StandardCharsets.UTF_8)
                .build());

        return new ResponseEntity<>(file.getFileData(), headers, HttpStatus.OK);
    }
}
