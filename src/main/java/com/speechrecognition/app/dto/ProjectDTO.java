package com.speechrecognition.app.dto;

import com.speechrecognition.app.model.Project;
import com.speechrecognition.app.model.ProjectStatus;

import java.time.format.DateTimeFormatter;

public class ProjectDTO {
    private Long id;
    private String name;
    private String folderName;
    private String createdAt;
    private String lastChanged;
    private String duration;
    private ProjectStatus status;
    private String summary;

    // Date formatters
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d/M/yyyy");

    public ProjectDTO() {}

    /**
     * Convert a Project entity to a ProjectDTO.
     * This avoids lazy-loading issues and only exposes what the frontend needs.
     */
    public static ProjectDTO fromEntity(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setStatus(project.getStatus());
        dto.setSummary(project.getSummary());
        dto.setDuration(project.getFormattedDuration());

        // Safely access folder name (folder can be null)
        if (project.getFolder() != null) {
            dto.setFolderName(project.getFolder().getName());
        } else {
            dto.setFolderName("—");
        }

        // Format dates
        if (project.getCreatedAt() != null) {
            dto.setCreatedAt(project.getCreatedAt().format(DATE_FORMAT));
        }
        if (project.getLastChanged() != null) {
            dto.setLastChanged(project.getLastChanged().format(DATE_FORMAT));
        }

        return dto;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getLastChanged() { return lastChanged; }
    public void setLastChanged(String lastChanged) { this.lastChanged = lastChanged; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}