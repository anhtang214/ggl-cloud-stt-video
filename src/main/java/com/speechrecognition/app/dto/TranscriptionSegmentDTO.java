package com.speechrecognition.app.dto;

import com.speechrecognition.app.model.TranscriptionSegment;

public class TranscriptionSegmentDTO {
    private Long id;
    private Integer segmentIndex;
    private String text;
    private Double startTimeSeconds;
    private Double endTimeSeconds;
    private String formattedStartTime;

    public TranscriptionSegmentDTO() {}

    public static TranscriptionSegmentDTO fromEntity(TranscriptionSegment segment) {
        TranscriptionSegmentDTO dto = new TranscriptionSegmentDTO();
        dto.setId(segment.getId());
        dto.setSegmentIndex(segment.getSegmentIndex());
        dto.setText(segment.getText());
        dto.setStartTimeSeconds(segment.getStartTimeSeconds());
        dto.setEndTimeSeconds(segment.getEndTimeSeconds());
        dto.setFormattedStartTime(formatTime(segment.getStartTimeSeconds()));
        return dto;
    }

    private static String formatTime(Double seconds) {
        if (seconds == null) return "00:00";
        int totalSeconds = (int) Math.floor(seconds);
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int secs = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        }
        return String.format("%02d:%02d", minutes, secs);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getSegmentIndex() { return segmentIndex; }
    public void setSegmentIndex(Integer segmentIndex) { this.segmentIndex = segmentIndex; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Double getStartTimeSeconds() { return startTimeSeconds; }
    public void setStartTimeSeconds(Double startTimeSeconds) { this.startTimeSeconds = startTimeSeconds; }

    public Double getEndTimeSeconds() { return endTimeSeconds; }
    public void setEndTimeSeconds(Double endTimeSeconds) { this.endTimeSeconds = endTimeSeconds; }

    public String getFormattedStartTime() { return formattedStartTime; }
    public void setFormattedStartTime(String formattedStartTime) { this.formattedStartTime = formattedStartTime; }
}