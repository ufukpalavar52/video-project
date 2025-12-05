package com.videoprocessor.service;

import java.io.IOException;

public interface StorageService {
    Boolean saveFile(String fullPath, byte[] data) throws IOException;
    byte[] getFile(String fullPath) throws IOException;
    void deleteFile(String fullPath) throws IOException;
}
