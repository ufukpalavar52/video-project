package com.videoprocessor.service;

import com.videoprocessor.constant.ErrorCode;
import com.videoprocessor.exception.CommonException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3SaveService implements StorageService {
    private final S3Client s3Client;


    @Override
    public Boolean saveFile(String fullPath, byte[] data) {
        Path path = Paths.get(fullPath);

        if (path.getFileName().toString().isEmpty()) {
            throw new CommonException(ErrorCode.INVALID_PATH);
        }

        String bucketName = path.getParent().toString();
        String fileName = path.getFileName().toString();

        createBucketIfNotExists(bucketName);
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .build(),
                RequestBody.fromBytes(data)
        );

        return true;
    }

    @Override
    public byte[] getFile(String fullPath) throws IOException {
        Path path = Paths.get(fullPath);
        if (path.getFileName().toString().isEmpty()) {
            throw new CommonException(ErrorCode.INVALID_PATH);
        }

        String bucketName = path.getParent().toString();
        String fileName = path.getFileName().toString();

        return s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .build()
        ).readAllBytes();
    }

    @Override
    public void deleteFile(String fullPath) throws IOException {
        Path path = Paths.get(fullPath);
        if (path.getFileName().toString().isEmpty()) {
            throw new CommonException(ErrorCode.INVALID_PATH);
        }
        String bucketName = path.getParent().toString();
        String fileName = path.getFileName().toString();
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build());
    }

    public void createBucketIfNotExists(String bucketName) {
        boolean exists = s3Client.listBuckets()
                .buckets()
                .stream()
                .anyMatch(b -> b.name().equals(bucketName));

        if (exists) {
            return;
        }

        try {
            s3Client.createBucket(
                    CreateBucketRequest.builder()
                            .bucket(bucketName)
                            .build()
            );

        } catch (BucketAlreadyOwnedByYouException | BucketAlreadyExistsException ignore) {
            log.warn("Bucket {} already exists", bucketName);
        }
    }

}
