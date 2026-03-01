package com.speechrecognition.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;
import java.io.IOException;

@Service
public class VideoProcessingService {
    @Autowired
    private CloudStorageService cloudStorageService;

    public VideoResult processVideo(MultipartFile videoFile, String title, String userId, String projectId) throws Exception {
        // Upload video to GCS immediately
        String videoUri = cloudStorageService.uploadVideoFile(videoFile, userId, projectId);

        File tempAudio = null;
        File tempVideo = File.createTempFile("video-", ".mp4");

        try {
            videoFile.transferTo(tempVideo);
            tempAudio = extractWavAudio(tempVideo);
            String audioUri = cloudStorageService.uploadAudioFile(tempAudio, userId, projectId);
            return new VideoResult(videoUri, audioUri, title);
        } finally {
            tempVideo.delete();
            if (tempAudio != null) tempAudio.delete();
        }
    }

    private File extractWavAudio(File videoFile) throws IOException, EncoderException {
        File audioFile = File.createTempFile("audio-", ".wav");
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("pcm_s16le");
        audio.setChannels(2);
        audio.setSamplingRate(44100);

        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setOutputFormat("wav");
        attrs.setAudioAttributes(audio);

        Encoder encoder = new Encoder();
        encoder.encode(new MultimediaObject(videoFile), audioFile, attrs);

        System.out.println("✓ Audio extracted: " + videoFile.getName() +
                " (" + (audioFile.length() / 1024 / 1024) + " MB)");
        return audioFile;
    }

    public static class VideoResult {
        public String videoUri;
        public String audioUri;
        public String title;

        public VideoResult(String videoUri, String audioUri, String title) {
            this.videoUri = videoUri;
            this.audioUri = audioUri;
            this.title = title;
        }
    }
}
