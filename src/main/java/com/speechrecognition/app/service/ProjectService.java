package com.speechrecognition.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.speechrecognition.app.model.Folder;
import com.speechrecognition.app.model.Project;
import com.speechrecognition.app.model.ProjectStatus;
import com.speechrecognition.app.model.User;
import com.speechrecognition.app.repository.ProjectRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {
	
	@Autowired
    private ProjectRepository projectRepository;
	
	public List<Project> getUserProjects(Long userId) {
        return projectRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
	
	public List<Project> getCompletedProjects(Long userId) {
        return projectRepository.findByUserIdAndStatus(
            userId, 
            ProjectStatus.COMPLETED
        );
    }
	
	public Project createProject(User user, String fileName) {
        Project project = new Project();
        project.setUser(user);
        project.setName(fileName);
        project.setStatus(ProjectStatus.PROCESSING);
        
        return projectRepository.save(project);
    }
	
	public void updateProject(Long projectId, ProjectUpdate update) {
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new RuntimeException("Project not found"));

		if (update.name != null)            project.setName(update.name);
		if (update.status != null)          project.setStatus(update.status);
		if (update.summary != null)         project.setSummary(update.summary);
		if (update.gcsUri != null)          project.setGcsUri(update.gcsUri);
		if (update.durationSeconds != null) project.setDurationSeconds(update.durationSeconds);
		if (update.folder != null)          project.setFolder(update.folder);

		projectRepository.save(project);
	}

	public static class ProjectUpdate {
		public String name;
		public ProjectStatus status;
		public String summary;
		public String gcsUri;
		public Integer durationSeconds;
		public Folder folder;
	}
	
	public void markAsFailed(Long projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("project not found"));
        
        project.setStatus(ProjectStatus.FAILED);
        project.setLastChanged(LocalDateTime.now());
        
        projectRepository.save(project);
    }
	
	public Long getUserProjectCount(Long userId) {
        return projectRepository.countByUserId(userId);
    }

	public Project getProjectById(Long id) {
		return projectRepository.findById(id).orElse(null);
	}
}
