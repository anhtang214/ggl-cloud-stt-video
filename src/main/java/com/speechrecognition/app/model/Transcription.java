package com.speechrecognition.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transcriptions")
public class Transcription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "folder_name")
    private String folderName;
    
    @Column(name = "gcs_uri")
    private String gcsUri;
    
    @Column(columnDefinition = "TEXT")
    private String transcript;
    
    @Column(columnDefinition = "TEXT")
    private String summary;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TranscriptionStatus status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_changed")
    private LocalDateTime lastChanged;
    
    // ⭐ Foreign Key - Liên kết với User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    public Transcription() {
        this.createdAt = LocalDateTime.now();
        this.lastChanged = LocalDateTime.now();
        this.status = TranscriptionStatus.PROCESSING;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }
    
    public String getGcsUri() { return gcsUri; }
    public void setGcsUri(String gcsUri) { this.gcsUri = gcsUri; }
    
    public String getTranscript() { return transcript; }
    public void setTranscript(String transcript) { this.transcript = transcript; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { 
        this.durationMinutes = durationMinutes; 
    }
    
    public TranscriptionStatus getStatus() { return status; }
    public void setStatus(TranscriptionStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastChanged() { return lastChanged; }
    public void setLastChanged(LocalDateTime lastChanged) { 
        this.lastChanged = lastChanged; 
    }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}