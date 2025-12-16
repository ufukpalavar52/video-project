package com.videoprocessor.repository;

import com.videoprocessor.model.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> getByTransactionId(String transactionId);

    Optional<Video> getByTransactionIdAndProcessType(String transactionId, String processType);

    List<Video> getByCreatedAtBeforeAndStatus(LocalDateTime createdAtBefore, String status);
}

