package com.speechrecognition.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.speechrecognition.app.model.Project;
import com.speechrecognition.app.model.ProjectStatus;
import com.speechrecognition.app.model.User;
import com.speechrecognition.app.service.ProjectService.ProjectUpdate;
import com.speechrecognition.app.service.SpeechService.TranscriptionResult;
import com.speechrecognition.app.service.VideoProcessingService.VideoResult;

@Service
public class UploadService {
    @Autowired
    private ProjectService projectService;

    @Autowired
    private VideoProcessingService videoProcessingService;

    @Autowired
    private SpeechService speechService;

    @Autowired
    private TranscriptionSegmentService segmentService;

    public void fullUploadPipeline(User user, MultipartFile videoFile, String projectTitle, String language,
            String language2) throws Exception {
        Project project = projectService.createProject(user, projectTitle);
        VideoResult videoResult = videoProcessingService.processVideo(
                videoFile,
                projectTitle,
                Long.toString(user.getId()),
                Long.toString(project.getId()));

        // Update attributes one by one in case of disruptions, more error handling will
        // be implemented later
        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.gcsUri = videoResult.videoUri;
        projectService.updateProject(project.getId(), projectUpdate);

        TranscriptionResult transcriptionResult = speechService.transcribeAndSummarize(videoResult.audioUri, language, language2);

        projectUpdate.summary = transcriptionResult.summary;
        projectService.updateProject(project.getId(), projectUpdate);

        segmentService.saveSegments(project, transcriptionResult.transcriptions);

        projectUpdate.status = ProjectStatus.COMPLETED;
        projectService.updateProject(project.getId(), projectUpdate);
    }
}
