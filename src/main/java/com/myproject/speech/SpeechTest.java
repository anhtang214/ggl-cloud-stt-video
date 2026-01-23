package com.myproject.speech;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.retrying.RetrySettings;
import com.google.api.gax.longrunning.OperationFuture;

import com.google.cloud.speech.v1.*;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.google.protobuf.ByteString;
import com.google.api.gax.retrying.RetrySettings;
import org.threeten.bp.Duration;

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

public class SpeechTest {
    private static final String PROJECT_ID = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
    
    public static void main(String[] args) {
        try {
        	
            System.out.println("Initializing Google Cloud Speech-to-Text...");

            // Load credentials
//            String credentialsPath = "speechtotest-484119-6e6f7f42dd69.json";  // Ha's credential
            String credentialsPath = "service-account.json";  // TA's credential
            ServiceAccountCredentials credentials;
            
            try (FileInputStream credentialsStream = new FileInputStream(credentialsPath)) {
                credentials = (ServiceAccountCredentials) ServiceAccountCredentials
                    .fromStream(credentialsStream)
                    .createScoped("https://www.googleapis.com/auth/cloud-platform");
                
                System.out.println("Credentials class = " + credentials.getClass().getName());
            }
            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();
            
            // Video demo
        	String videoFile = "demo-be9-day28.mp4";
        	String wavFile = "extracted-audio.wav";
        	extractAudio(videoFile, wavFile);
        	// Upload WAV file to GCS
        	Path audioPath = Paths.get(wavFile);
        	byte[] audioBytes = Files.readAllBytes(audioPath);

        	// Define bucket and file name
        	String bucketName = "anh-first-bucket";
        	String fileName = "audio/" + audioPath.getFileName().toString();

        	// Create blob reference
        	BlobId blobId = BlobId.of(bucketName, fileName);
        	BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        	// Upload the bytes
        	storage.create(blobInfo, audioBytes);

        	// Get the GCS URI
        	String gcsAudioUri = "gs://" + bucketName + "/" + fileName;
        	System.out.println("Uploaded to: " + gcsAudioUri);
        	
        	Files.deleteIfExists(audioPath); // delete the temporary local file
            
            // Create SpeechSettings with EXTENDED timeout
            SpeechSettings.Builder settingsBuilder = SpeechSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials));

            // Configure timeout for long running operations
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

            SpeechSettings speechSettings = settingsBuilder.build();

            // Speech client
            try (SpeechClient speechClient = SpeechClient.create(speechSettings)) {

//                String gcsUri = "gs://my-speech-audio-test-files/test.wav";
//                String localUri = "demo-be9-day28.wav";
                String transcript = processFromCloudStorage(speechClient, gcsAudioUri);
//                String transcript = processLocalFile(speechClient, localUri);
                
                // Generate summary
                if (transcript != null && !transcript.isEmpty()) {
                    System.out.println("\n=== GENERATING SUMMARY ===");
                    String summary = summarizeWithGemini(transcript, credentials);
                    
                    System.out.println("\n=== SUMMARY ===");
                    System.out.println(summary);
                }
            }

            System.out.println("\nDone!");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Process audio from Cloud Storage URI and return full transcript
    private static String processFromCloudStorage(SpeechClient speechClient, String gcsUri) throws Exception {
        System.out.println("Processing audio from Cloud Storage: " + gcsUri);
        
        // Configure recognition
        RecognitionConfig config = RecognitionConfig.newBuilder()
            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
//            .setSampleRateHertz(44100)
            .setAudioChannelCount(2)
            .setLanguageCode("vi-VN")
            .addAlternativeLanguageCodes("en-US")
            .setEnableAutomaticPunctuation(true)
            .build();
        
        RecognitionAudio audio = RecognitionAudio.newBuilder()
            .setUri(gcsUri)
            .build();
        
        System.out.println("Waiting for transcription...");
//        System.out.println("This may take 30-60 minutes for long audio files. Please be patient.");
        OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> operation =
            speechClient.longRunningRecognizeAsync(config, audio);
        
        System.out.println("Operation started. Waiting for completion...");
        
        // Wait up to 2 hours for completion
        LongRunningRecognizeResponse response = operation.get(2, TimeUnit.HOURS);
        
        // Collect all transcript segments
        StringBuilder fullTranscript = new StringBuilder();
        displayResults(response.getResultsList(), fullTranscript);
        
        return fullTranscript.toString().trim();
    }
    
    // Process local audio file (< 10MB) and return full transcript
    private static String processLocalFile(SpeechClient speechClient, String audioFilePath) throws Exception {
        System.out.println("Reading local audio file: " + audioFilePath);
        
        Path path = Paths.get(audioFilePath);
        byte[] audioData = Files.readAllBytes(path);
        ByteString audioBytes = ByteString.copyFrom(audioData);
        
        // Auto-detect channels
        int channels = detectAudioChannels(audioFilePath);
        
        System.out.println("Processing audio...");
        
        RecognitionConfig config = RecognitionConfig.newBuilder()
            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
            .setAudioChannelCount(channels)
            .setLanguageCode("vi-VN")
            .addAlternativeLanguageCodes("en-US")
            .setEnableAutomaticPunctuation(true)
            .build();

        RecognitionAudio audio = RecognitionAudio.newBuilder()
            .setContent(audioBytes)
            .build();

        RecognizeResponse response = speechClient.recognize(config, audio);
        
        // Collect all transcript segments
        StringBuilder fullTranscript = new StringBuilder();
        displayResults(response.getResultsList(), fullTranscript);
        
        return fullTranscript.toString().trim();
    }
    
    // Display results and collect full transcript
    private static void displayResults(java.util.List<SpeechRecognitionResult> results, 
                                       StringBuilder fullTranscript) {
        System.out.println("\n=== TRANSCRIPTION RESULTS ===");
        System.out.println("Number of segments: " + results.size());

        if (results.isEmpty()) {
            System.out.println("No speech detected in the audio file.");
        } else {
            int resultIndex = 1;
            for (SpeechRecognitionResult result : results) {
                if (result.getAlternativesCount() > 0) {
                    SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                    String text = alternative.getTranscript();
                    
                    System.out.println("\n--- Segment " + resultIndex + " ---");
                    System.out.println("Transcript: " + text);
                    System.out.println("Confidence: " + alternative.getConfidence());
                    
                    // Add to full transcript
                    if (fullTranscript != null) {
                        fullTranscript.append(text).append(" ");
                    }
                    
                    resultIndex++;
                }
            }
        }
    }
    
 // Summarize transcript using Gemini
    private static String summarizeWithGemini(String transcript, GoogleCredentials credentials) {
        try {
            System.out.println("Connecting to Gemini API...");
            
            // Build VertexAI with credentials
            try (VertexAI vertexAI = new VertexAI.Builder()
                    .setProjectId(PROJECT_ID)
                    .setLocation("us-central1")
                    .setCredentials(credentials)
                    .build()) {
                
                GenerativeModel model = new GenerativeModel("gemini-2.0-flash-001", vertexAI);
                
                String prompt = "Summarize the transcript with concise bullet points that capture main content." +
                               "Keep the original content and language from the transcript:\n\n" + transcript;
                
                System.out.println("Generating summary...");
                GenerateContentResponse response = model.generateContent(prompt);
                String summary = ResponseHandler.getText(response);
                
                return summary;
            }
        } catch (Exception e) {
            System.out.println("Error generating summary: " + e.getMessage());
            e.printStackTrace();
            return "Could not generate summary. Error: " + e.getMessage();
        }
    }

    
    // Auto-detect audio channels from WAV file
    private static int detectAudioChannels(String audioFilePath) {
        try {
            Path path = Paths.get(audioFilePath);
            byte[] header = Files.readAllBytes(path);
            
            if (header.length > 23 && 
                header[0] == 'R' && header[1] == 'I' && 
                header[2] == 'F' && header[3] == 'F') {
                
                int channels = (header[22] & 0xFF) | ((header[23] & 0xFF) << 8);
                System.out.println("Detected: " + (channels == 1 ? "mono" : "stereo"));
                return channels;
            }
        } catch (Exception e) {
            System.out.println("Could not detect channels, using mono");
        }
        return 1;
    }
    
    
    private static void extractAudio(String videoFile, String wavFile) throws Exception {
        File source = new File(videoFile);
        File target = new File(wavFile);

        if (!source.exists()) {
            throw new RuntimeException("Video file not found: " + videoFile);
        }

        System.out.println("Source file: " + videoFile + " (" + (source.length() / 1024 / 1024) + " MB)");

        // Configure audio settings for WAV
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("pcm_s16le");  
        audio.setChannels(2);          
        audio.setSamplingRate(44100);  

        // Configure encoding
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setOutputFormat("wav");
        attrs.setAudioAttributes(audio);

        // Extract audio
        Encoder encoder = new Encoder();
        System.out.println("Converting to WAV...");
        encoder.encode(new MultimediaObject(source), target, attrs);

        System.out.println("✓ Audio extracted: " + wavFile + " (" + (target.length() / 1024 / 1024) + " MB)");
    }
}