package com.app.service;

import com.app.model.File;
import com.app.repo.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * FileService handles file upload and retrieval operations.
 * It provides methods to save files and retrieve them by ID.
 */
@Service
public class FileService {

    /**
     * Repository for managing File entities.
     * It provides methods to save and retrieve files from the database.
     */
    @Autowired
    private FileRepository fileRepository;

    /**
     * Saves a file uploaded via MultipartFile.
     * It extracts the file data, filename, and MIME type, then saves it to the database.
     *
     * @param multipartFile the file to be saved
     * @return the saved File entity
     * @throws IOException if an error occurs while reading the file data
     */
    public File saveFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }
        String mimeType = multipartFile.getContentType();
        File file = new File();
        file.setFilename(multipartFile.getOriginalFilename());
        file.setFileData(multipartFile.getBytes());
        file.setUploadedAt(LocalDateTime.now());
        file.setMimeType(mimeType);
        return fileRepository.save(file);
    }

    /**
     * Retrieves a file by its ID.
     * It looks up the file in the database and returns it if found.
     *
     * @param id the unique identifier of the file
     * @return the File entity if found, or null if not found
     */
    public File getFileById(Long id) {
        return fileRepository.findById(id).orElse(null);
    }
}
