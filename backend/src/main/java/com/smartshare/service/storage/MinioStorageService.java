package com.smartshare.service.storage;

import com.smartshare.config.minio.MinioConfig;
import com.smartshare.exception.storage.StorageException;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @Override
    public void uploadFile(String objectName, InputStream stream, long size, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(objectName)
                            .stream(stream, size, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to upload object: " + objectName, e);
        }
    }

    @Override
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to download object: " + objectName, e);
        }
    }

    @Override
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to delete object: " + objectName, e);
        }
    }

    @Override
    public boolean objectExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String generateObjectUrl(String objectName) {
        return String.format("%s/%s/%s", minioConfig.getUrl(), minioConfig.getBucket(), objectName);
    }
}
