package com.videoprocessor.repository;

import com.videoprocessor.model.entity.GifVideoErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GifVideoErrorLogRepository extends JpaRepository<GifVideoErrorLog, Long> {
}
