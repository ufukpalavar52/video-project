package com.videoprocessor.service.impl;

import com.videoprocessor.service.intf.StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3SaveServiceImplTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3SaveServiceImpl s3SaveService;

    @Test
    void saveFile_WhenBucketDoesNotExist_ShouldCreateBucketAndPutObject() {
        byte[] data = "dummy-data".getBytes();

        when(s3Client.listBuckets()).thenReturn(
                ListBucketsResponse.builder().buckets(List.of()).build()
        );

        Boolean result = s3SaveService.saveFile("my-bucket/video.mp4", data);

        assertTrue(result);

        ArgumentCaptor<CreateBucketRequest> createBucketCaptor = ArgumentCaptor.forClass(CreateBucketRequest.class);
        verify(s3Client).createBucket(createBucketCaptor.capture());
        assertEquals("my-bucket", createBucketCaptor.getValue().bucket());

        ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(putCaptor.capture(), any(RequestBody.class));
        assertEquals("my-bucket", putCaptor.getValue().bucket());
        assertEquals("video.mp4", putCaptor.getValue().key());
    }

    @Test
    void saveFile_WhenBucketExists_ShouldNotCreateBucket() {
        byte[] data = "dummy-data".getBytes();

        when(s3Client.listBuckets()).thenReturn(
                ListBucketsResponse.builder()
                        .buckets(List.of(Bucket.builder().name("my-bucket").build()))
                        .build()
        );

        Boolean result = s3SaveService.saveFile("my-bucket/video.mp4", data);

        assertTrue(result);
        verify(s3Client, never()).createBucket(any(CreateBucketRequest.class));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void saveFile_WhenCreateBucketThrowsAlreadyExists_ShouldStillPutObject() {
        byte[] data = "dummy-data".getBytes();

        when(s3Client.listBuckets()).thenReturn(
                ListBucketsResponse.builder().buckets(List.of()).build()
        );
        when(s3Client.createBucket(any(CreateBucketRequest.class)))
                .thenThrow(BucketAlreadyExistsException.builder().message("exists").build());

        Boolean result = s3SaveService.saveFile("my-bucket/video.mp4", data);

        assertTrue(result);
        verify(s3Client).createBucket(any(CreateBucketRequest.class));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void getFile_ShouldReturnBytes() throws Exception {
        byte[] expected = "file-content".getBytes();

        @SuppressWarnings("unchecked")
        ResponseInputStream<GetObjectResponse> stream = mock(ResponseInputStream.class);

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(stream);
        when(stream.readAllBytes()).thenReturn(expected);

        byte[] result = s3SaveService.getFile("my-bucket/video.mp4");

        assertArrayEquals(expected, result);

        ArgumentCaptor<GetObjectRequest> getCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(s3Client).getObject(getCaptor.capture());
        assertEquals("my-bucket", getCaptor.getValue().bucket());
        assertEquals("video.mp4", getCaptor.getValue().key());
    }

    @Test
    void getFile_WhenReadFails_ShouldPropagateIOException() throws Exception {
        @SuppressWarnings("unchecked")
        ResponseInputStream<GetObjectResponse> stream = mock(ResponseInputStream.class);

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(stream);
        when(stream.readAllBytes()).thenThrow(new IOException("read failed"));

        IOException ex = assertThrows(IOException.class,
                () -> s3SaveService.getFile("my-bucket/video.mp4"));

        assertEquals("read failed", ex.getMessage());
    }

    @Test
    void deleteFile_ShouldCallDeleteObjectWithCorrectBucketAndKey() throws Exception {
        s3SaveService.deleteFile("my-bucket/video.mp4");

        ArgumentCaptor<DeleteObjectRequest> deleteCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(deleteCaptor.capture());

        assertEquals("my-bucket", deleteCaptor.getValue().bucket());
        assertEquals("video.mp4", deleteCaptor.getValue().key());
    }
}
