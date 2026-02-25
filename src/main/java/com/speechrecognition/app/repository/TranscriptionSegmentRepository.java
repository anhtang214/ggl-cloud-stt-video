package com.speechrecognition.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.speechrecognition.app.model.TranscriptionSegment;

@Repository
public interface TranscriptionSegmentRepository extends JpaRepository<TranscriptionSegment, Long> {
    List<TranscriptionSegment> findByProjectIdOrderBySegmentIndexAsc(Long projectId);
}
