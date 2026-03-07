package com.speechrecognition.app.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.retrying.RetrySettings;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.speech.v1.*;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.threeten.bp.Duration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SpeechService {

    private static final Map<String, String> LANGUAGE_CODES = Map.of(
            "English", "en-US",
            "Vietnamese", "vi-VN");

    @Value("${gcp.project-id}")
    private String projectId;

    private final SpeechSettings speechSettings;
    private final ServiceAccountCredentials credentials;

    public SpeechService(ServiceAccountCredentials credentials) throws Exception {
        this.credentials = credentials;

        SpeechSettings.Builder settingsBuilder = SpeechSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials));

        settingsBuilder.longRunningRecognizeOperationSettings()
                .setPollingAlgorithm(
                        com.google.api.gax.longrunning.OperationTimedPollAlgorithm.create(
                                RetrySettings.newBuilder()
                                        .setInitialRetryDelay(Duration.ofSeconds(5))
                                        .setRetryDelayMultiplier(1.5)
                                        .setMaxRetryDelay(Duration.ofSeconds(45))
                                        .setInitialRpcTimeout(Duration.ZERO)
                                        .setRpcTimeoutMultiplier(1.0)
                                        .setMaxRpcTimeout(Duration.ZERO)
                                        .setTotalTimeout(Duration.ofHours(2))
                                        .build()));

        this.speechSettings = settingsBuilder.build();
    }

    public TranscriptionResult transcribeAndSummarize(String gcsAudioUri, String language, String language2)
            throws Exception {
        try (SpeechClient speechClient = SpeechClient.create(speechSettings)) {
            String languageCode = LANGUAGE_CODES.getOrDefault(language, "en-US");
            String altLanguageCode = LANGUAGE_CODES.getOrDefault(language2, "en-US");
            List<SpeechRecognitionResult> results = processFromCloudStorage(speechClient, gcsAudioUri, languageCode,
                    altLanguageCode);
            StringBuilder fullTranscript = new StringBuilder();

            if (results.isEmpty()) {
                System.out.println("No speech detected in the audio file.");
            } else {
                for (SpeechRecognitionResult result : results) {
                    if (result.getAlternativesCount() > 0) {
                        String text = result.getAlternativesList().get(0).getTranscript();
                        fullTranscript.append(text).append(" ");
                    }
                }
            }
            String summary = "";
            if (fullTranscript != null && !fullTranscript.isEmpty()) {
                summary = summarizeWithGemini(fullTranscript, credentials);
            }
            return new TranscriptionResult(results, summary);
        }
    }

    private static List<SpeechRecognitionResult> processFromCloudStorage(SpeechClient speechClient, String gcsUri,
            String languageCode, String altLanguageCode) throws Exception {
        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setAudioChannelCount(2)
                .setLanguageCode(languageCode)
                .addAlternativeLanguageCodes(altLanguageCode)
                .setEnableAutomaticPunctuation(true)
                .setEnableWordTimeOffsets(true)
                .build();

        RecognitionAudio audio = RecognitionAudio.newBuilder()
                .setUri(gcsUri)
                .build();

        OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> operation = speechClient
                .longRunningRecognizeAsync(config, audio);

        LongRunningRecognizeResponse response = operation.get(2, TimeUnit.HOURS);

        return response.getResultsList();
    }

    private String summarizeWithGemini(StringBuilder transcript, GoogleCredentials credentials) {
        try {
            try (VertexAI vertexAI = new VertexAI.Builder()
                    .setProjectId(projectId)
                    .setLocation("us-central1")
                    .setCredentials(credentials)
                    .build()) {

                GenerativeModel model = new GenerativeModel("gemini-2.0-flash-001", vertexAI);

                String prompt = "Summarize the transcript with concise bullet points that capture main content. " +
                        "Keep the original content and language from the transcript:\n\n" + transcript;

                GenerateContentResponse response = model.generateContent(prompt);
                return ResponseHandler.getText(response);
            }
        } catch (Exception e) {
            System.out.println("Error generating summary: " + e.getMessage());
            return "Could not generate summary. Error: " + e.getMessage();
        }
    }

    public static class TranscriptionResult {
        public final String summary;
        public final List<SpeechRecognitionResult> transcriptions;

        public TranscriptionResult(List<SpeechRecognitionResult> transcriptions, String summary) {
            this.transcriptions = transcriptions;
            this.summary = summary;
        }
    }
}
