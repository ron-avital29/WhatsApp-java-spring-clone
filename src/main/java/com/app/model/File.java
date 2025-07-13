package com.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

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

    public File() {}

    public void setId(Long id) { this.id = id; }

    public void setFilename(String filename) { this.filename = filename; }

    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

}

