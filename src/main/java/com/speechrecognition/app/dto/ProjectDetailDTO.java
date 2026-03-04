package com.speechrecognition.app.dto;

import com.speechrecognition.app.model.Project;
import java.util.List;

public class ProjectDetailDTO {
    private Long id;
    private String name;
    private String status;
    private String summary;
    private String formattedDuration;
    private String folderName;
    private String videoUrl;
    private List<TranscriptionSegmentDTO> segments;

    public ProjectDetailDTO() {}

    public static ProjectDetailDTO fromEntity(Project project, String videoUrl,
                                               List<TranscriptionSegmentDTO> segments) {
        ProjectDetailDTO dto = new ProjectDetailDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setStatus(project.getStatus() != null ? project.getStatus().name() : null);
        dto.setSummary(project.getSummary());
        dto.setFormattedDuration(project.getFormattedDuration());
        dto.setVideoUrl(videoUrl);
        dto.setSegments(segments);

        try {
            if (project.getFolder() != null) {
                dto.setFolderName(project.getFolder().getName());
            }
        } catch (Exception e) {
            dto.setFolderName(null);
        }

        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getFormattedDuration() { return formattedDuration; }
    public void setFormattedDuration(String formattedDuration) { this.formattedDuration = formattedDuration; }

    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public List<TranscriptionSegmentDTO> getSegments() { return segments; }
    public void setSegments(List<TranscriptionSegmentDTO> segments) { this.segments = segments; }
}