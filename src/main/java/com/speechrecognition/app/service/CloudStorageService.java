package com.speechrecognition.app.service;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;

@Service
public class CloudStorageService {
    private final Storage storage;

    @Value("${gcp.bucket-name}")
    private String bucketName;

    public CloudStorageService(ServiceAccountCredentials credentials) {
        this.storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }

    public String uploadVideoFile(MultipartFile file, String userID, String projectID) throws Exception {
        String fileName = "video/" + userID + "-" + projectID;
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();
        storage.create(blobInfo, file.getBytes());
        return "gs://" + bucketName + "/" + fileName;
    }

    public String uploadAudioFile(File file, String userID, String projectID) throws Exception {
        String fileName = "audio/" + userID + "-" + projectID;
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        try (FileInputStream fis = new FileInputStream(file)) {
            storage.create(blobInfo, fis.readAllBytes());
        }
        return "gs://" + bucketName + "/" + fileName;
    }

    public String getSignedUrl(String gcsUri) throws Exception {
        String fileName = gcsUri.replace("gs://" + bucketName + "/", "");
        BlobId blobId = BlobId.of(bucketName, fileName);
        Blob blob = storage.get(blobId);
        return blob.signUrl(1, TimeUnit.HOURS).toString();
    }
}
