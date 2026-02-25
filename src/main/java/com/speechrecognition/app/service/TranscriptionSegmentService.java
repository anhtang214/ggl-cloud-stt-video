package com.speechrecognition.app.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.cloud.speech.v1.WordInfo;
import com.google.protobuf.Duration;
import com.speechrecognition.app.model.Project;
import com.speechrecognition.app.model.TranscriptionSegment;
import com.speechrecognition.app.repository.TranscriptionSegmentRepository;

@Service
public class TranscriptionSegmentService {
    @Autowired
    private TranscriptionSegmentRepository segmentRepository;

    public List<TranscriptionSegment> getSegmentsByProject(Long projectId) {
        return segmentRepository.findByProjectIdOrderBySegmentIndexAsc(projectId);
    }

    public void saveSegments(Project project, List<SpeechRecognitionResult> transcriptions) {
        List<TranscriptionSegment> segments = new ArrayList<>();
        if (transcriptions.isEmpty()) {
            System.out.println("No speech detected in the audio file.");
        } else {
            int resultIndex = 0;
            for (SpeechRecognitionResult transcript : transcriptions) {
                if (transcript.getAlternativesCount() > 0) {
                    SpeechRecognitionAlternative alternative = transcript.getAlternativesList().get(0);
                    String text = alternative.getTranscript();
                    List<WordInfo> words = alternative.getWordsList();
                    if (words.isEmpty()) continue;

                    Duration start = words.get(0).getStartTime();
                    Duration end = words.get(words.size() - 1).getEndTime();
                    Double startTime = start.getSeconds() + start.getNanos() / 1_000_000_000.0;
                    Double endTime = end.getSeconds() + end.getNanos() / 1_000_000_000.0;

                    segments.add(new TranscriptionSegment(resultIndex, text, startTime, endTime, project));
                    resultIndex++;
                }
            }
        }
        segmentRepository.saveAll(segments);
    }
}
