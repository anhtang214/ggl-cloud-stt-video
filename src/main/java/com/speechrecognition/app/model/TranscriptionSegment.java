package com.speechrecognition.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "transcription_segments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"project_id", "segment_index"})
})
public class TranscriptionSegment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "segment_index")
    private Integer segmentIndex;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(name = "start_time_seconds")
    private Double startTimeSeconds;

    @Column(name = "end_time_seconds")
    private Double endTimeSeconds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    public TranscriptionSegment() {}

    public TranscriptionSegment(Integer segmentIndex, String text, Double startTimeSeconds, Double endTimeSeconds,
            Project project) {
        this.segmentIndex = segmentIndex;
        this.text = text;
        this.startTimeSeconds = startTimeSeconds;
        this.endTimeSeconds = endTimeSeconds;
        this.project = project;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSegmentIndex() {
        return segmentIndex;
    }

    public void setSegmentIndex(Integer segmentIndex) {
        this.segmentIndex = segmentIndex;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Double getStartTimeSeconds() {
        return startTimeSeconds;
    }

    public void setStartTimeSeconds(Double startTimeSeconds) {
        this.startTimeSeconds = startTimeSeconds;
    }

    public Double getEndTimeSeconds() {
        return endTimeSeconds;
    }

    public void setEndTimeSeconds(Double endTimeSeconds) {
        this.endTimeSeconds = endTimeSeconds;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    
}
