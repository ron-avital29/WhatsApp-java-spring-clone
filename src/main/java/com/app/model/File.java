package com.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a file stored in the application.
 * Contains metadata such as filename, MIME type, and the file data itself.
 */
@Entity
@Table(name = "files")
public class File implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    private String filename;
    @Getter
    private String mimeType;

    @Getter
    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] fileData;

    private LocalDateTime uploadedAt;

    /**
     * Default constructor for JPA.
     */
    public File() {}

    /**
     * Constructs a File object with the specified filename, file data, and MIME type.
     *
     * @param id  the unique identifier of the file
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Constructs a File object with the specified filename, file data, and MIME type.
     *
     * @param filename the name of the file
     */
    public void setFilename(String filename) { this.filename = filename; }

    /**
     * Constructs a File object with the specified file data and MIME type.
     *
     * @param fileData the byte array containing the file data
     */
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    /**
     * Constructs a File object with the specified uploaded time.
     *
     * @param uploadedAt the time when the file was uploaded
     */
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    /**
     * Constructs a File object with the specified MIME type.
     *
     * @param mimeType the MIME type of the file
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

}

