package com.speechrecognition.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.speechrecognition.app.model.Project;
import com.speechrecognition.app.model.ProjectStatus;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
   
    List<Project> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Project> findByUserIdAndStatus(Long userId, ProjectStatus status);
    Long countByUserId(Long userId);
}