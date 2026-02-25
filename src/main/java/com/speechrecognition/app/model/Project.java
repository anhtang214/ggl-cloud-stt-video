package com.speechrecognition.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "file_name"})
})
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "gcs_uri")
    private String gcsUri;
    
    @Column(columnDefinition = "TEXT")
    private String summary;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProjectStatus status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_changed")
    private LocalDateTime lastChanged;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")  // Nullable - projects can be in root
    private Folder folder;
    
    public Project() {
        this.createdAt = LocalDateTime.now();
        this.lastChanged = LocalDateTime.now();
        this.status = ProjectStatus.PROCESSING;
    }

    @PreUpdate
    public void preUpdate() {
        this.lastChanged = LocalDateTime.now();
    }

    /**
     * Get duration formatted as "MM:SS" or "HH:MM:SS"
     */
    public String getFormattedDuration() {
        if (durationSeconds == null) return "0:00";
        
        int hours = durationSeconds / 3600;
        int minutes = (durationSeconds % 3600) / 60;
        int seconds = durationSeconds % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%d:%02d", minutes, seconds);
    }

    public boolean isProcessingComplete() {
        return status == ProjectStatus.COMPLETED || status == ProjectStatus.FAILED;
    }

    public boolean isSuccessful() {
        return status == ProjectStatus.COMPLETED;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGcsUri() {
        return gcsUri;
    }

    public void setGcsUri(String gcsUri) {
        this.gcsUri = gcsUri;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastChanged() {
        return lastChanged;
    }

    public void setLastChanged(LocalDateTime lastChanged) {
        this.lastChanged = lastChanged;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }
}