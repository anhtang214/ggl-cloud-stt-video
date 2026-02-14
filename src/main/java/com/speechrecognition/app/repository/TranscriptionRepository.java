package com.speechrecognition.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.speechrecognition.app.model.Transcription;
import com.speechrecognition.app.model.TranscriptionStatus;
import java.util.List;

@Repository
public interface TranscriptionRepository extends JpaRepository<Transcription, Long> {
   
    List<Transcription> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Transcription> findByUserIdAndStatus(Long userId, TranscriptionStatus status);
    Long countByUserId(Long userId);
}