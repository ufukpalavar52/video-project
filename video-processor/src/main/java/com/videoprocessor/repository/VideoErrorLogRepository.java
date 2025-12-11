package com.videoprocessor.repository;

import com.videoprocessor.model.entity.VideoErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoErrorLogRepository extends JpaRepository<VideoErrorLog, Long> {
}
