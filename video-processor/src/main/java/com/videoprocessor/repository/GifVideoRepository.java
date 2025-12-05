package com.videoprocessor.repository;

import com.videoprocessor.model.entity.GifVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GifVideoRepository  extends JpaRepository<GifVideo, Long> {
    Optional<GifVideo> getByTransactionId(String transactionId);

    List<GifVideo> getByCreatedAtBeforeAndStatus(LocalDateTime createdAtBefore, String status);
}

