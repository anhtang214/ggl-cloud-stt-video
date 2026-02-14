package com.speechrecognition.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.speechrecognition.app.model.Transcription;
import com.speechrecognition.app.model.TranscriptionStatus;
import com.speechrecognition.app.model.User;
import com.speechrecognition.app.repository.TranscriptionRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TranscriptionService {
	
	@Autowired
    private TranscriptionRepository transcriptionRepository;
	
	public List<Transcription> getUserTranscriptions(Long userId) {
        return transcriptionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
	
	public List<Transcription> getCompletedTranscriptions(Long userId) {
        return transcriptionRepository.findByUserIdAndStatus(
            userId, 
            TranscriptionStatus.COMPLETED
        );
    }
	
	public Transcription createTranscription(User user, String fileName, String folderName) {
        Transcription transcription = new Transcription();
        transcription.setUser(user);
        transcription.setFileName(fileName);
        transcription.setName(fileName);
        transcription.setFolderName(folderName);
        transcription.setStatus(TranscriptionStatus.PROCESSING);
        
        return transcriptionRepository.save(transcription);
    }
	
	public void updateTranscriptionResult(Long transcriptionId, String transcript, String summary, String gcsUri, Integer duration) {
		Transcription transcription = transcriptionRepository.findById(transcriptionId).orElseThrow(() -> new RuntimeException("Transcription not found"));
		transcription.setTranscript(transcript);
		transcription.setSummary(summary);
		transcription.setGcsUri(gcsUri);
		transcription.setDurationMinutes(duration);
		transcription.setStatus(TranscriptionStatus.COMPLETED);
		transcription.setLastChanged(LocalDateTime.now());

		transcriptionRepository.save(transcription);
	}
	
	public void markAsFailed(Long transcriptionId) {
        Transcription transcription = transcriptionRepository.findById(transcriptionId)
            .orElseThrow(() -> new RuntimeException("Transcription not found"));
        
        transcription.setStatus(TranscriptionStatus.FAILED);
        transcription.setLastChanged(LocalDateTime.now());
        
        transcriptionRepository.save(transcription);
    }
	
	public Long getUserTranscriptionCount(Long userId) {
        return transcriptionRepository.countByUserId(userId);
    }
}
