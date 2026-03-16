package com.videoprocessor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.videoprocessor.constant.VideoProcessType;
import com.videoprocessor.model.entity.Video;
import com.videoprocessor.model.request.VideoRequest;
import com.videoprocessor.service.intf.VideoService;
import com.videoprocessor.util.StrUtils;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VideoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VideoService videoService;

    @Mock
    private Validator validator;

    @InjectMocks
    private VideoController videoController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(videoController).build();
    }

    @Test
    void uploadVideo_WithValidData_ShouldReturnOkAndVideoObject() throws Exception {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-video.mp4",
                "video/mp4",
                "dummy video content".getBytes()
        );

        Video mockResponseVideo = new Video();

        when(validator.validate(any(VideoRequest.class))).thenReturn(Collections.emptySet());
        when(videoService.save(any(VideoRequest.class), any())).thenReturn(mockResponseVideo);

        // Act & Assert
        mockMvc.perform(multipart(getUrl("/upload"))
                        .file(mockFile)
                        .param("startTime", "0")
                        .param("endTime", "15")
                        .param("processType", "CUT"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getTransaction_ShouldReturnOkAndVideoObject() throws Exception {
        String transactionId = StrUtils.UUID();
        Video mockResponseVideo = new Video();
        mockResponseVideo.setTransactionId(transactionId);
        when(videoService.getVideoTransactionId(any(String.class))).thenReturn(mockResponseVideo);
        mockMvc.perform(get(getUrl(""))
                        .param("transactionId", transactionId))
                .andExpect(status().isOk());
    }

    @Test
    void uploadUrlVideo_WithValidRequest_ShouldReturnOkAndVideo() throws Exception {
        VideoRequest validRequest = VideoRequest.builder()
                .url("https://example.com/sample-video.mp4")
                .startTime(0)
                .endTime(15)
                .processType(VideoProcessType.GIF)
                .build();

        Video mockVideo = new Video();
        when(videoService.save(any(VideoRequest.class))).thenReturn(mockVideo);


        mockMvc.perform(post(getUrl("/url"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void download_WithMp4Video_ShouldReturnFileAndMp4Headers() throws Exception {
        String transactionId = StrUtils.UUID();
        String outputPath = "/server/media/final-video.mp4";
        byte[] dummyFileContent = "fake mp4 bytes".getBytes();

        Video mockVideo = new Video();
        mockVideo.setOutputPath(outputPath);
        mockVideo.setProcessType("CUT");

        when(videoService.getVideoTransactionId(transactionId)).thenReturn(mockVideo);
        when(videoService.getFile(outputPath)).thenReturn(dummyFileContent);

        mockMvc.perform(get(getUrl("/download/{transactionId}"), transactionId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"final-video.mp4\""))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "video/mp4"))
                .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, String.valueOf(dummyFileContent.length)))
                .andExpect(content().bytes(dummyFileContent));
    }

    @Test
    void download_WithGifVideo_ShouldReturnFileAndGifHeaders() throws Exception {
        // Arrange
        String transactionId = StrUtils.UUID();
        String outputPath = "animation.gif";
        byte[] dummyFileContent = "fake gif bytes".getBytes();

        Video mockVideo = new Video();
        mockVideo.setOutputPath(outputPath);
        mockVideo.setProcessType("GIF");

        when(videoService.getVideoTransactionId(transactionId)).thenReturn(mockVideo);
        when(videoService.getFile(outputPath)).thenReturn(dummyFileContent);

        // Act & Assert
        mockMvc.perform(get(getUrl("/download/{transactionId}"), transactionId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"animation.gif\""))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_GIF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, String.valueOf(dummyFileContent.length)))
                .andExpect(content().bytes(dummyFileContent));
    }


    String getUrl(String path) {
        String apiPrefix = "/api/video";
        return String.format("%s%s", apiPrefix, path);
    }
}
