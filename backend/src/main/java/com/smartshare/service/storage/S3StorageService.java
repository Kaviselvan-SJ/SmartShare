package com.smartshare.service.storage;

import com.smartshare.config.s3.S3Config;
import com.smartshare.exception.storage.StorageException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;

/**
 * AWS S3 implementation of {@link StorageService}.
 * <p>
 * Objects are stored using the file hash as the S3 object key, which ensures
 * that deduplicated files share the same physical object and that versioned
 * files with different hashes always get their own distinct key.
 */
@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client s3Client;
    private final S3Config s3Config;

    /**
     * Uploads a file to S3.
     *
     * @param objectName  the S3 object key (file hash)
     * @param stream      input stream of the file content
     * @param size        byte length of the stream
     * @param contentType MIME type of the file
     */
    @Override
    public void uploadFile(String objectName, InputStream stream, long size, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectName)
                    .contentType(contentType)
                    .contentLength(size)
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(stream, size));
            logger.info("Uploaded object '{}' to S3 bucket '{}'", objectName, s3Config.getBucketName());
        } catch (Exception e) {
            throw new StorageException("Failed to upload object to S3: " + objectName, e);
        }
    }

    /**
     * Downloads a file from S3 and returns a streaming {@link InputStream}.
     *
     * @param objectName the S3 object key (file hash)
     * @return an {@link InputStream} backed by the S3 response
     */
    @Override
    public InputStream downloadFile(String objectName) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectName)
                    .build();

            logger.info("Downloading object '{}' from S3 bucket '{}'", objectName, s3Config.getBucketName());
            return s3Client.getObject(request);
        } catch (Exception e) {
            throw new StorageException("Failed to download object from S3: " + objectName, e);
        }
    }

    /**
     * Deletes an object from S3.
     *
     * @param objectName the S3 object key (file hash)
     */
    @Override
    public void deleteFile(String objectName) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectName)
                    .build();

            s3Client.deleteObject(request);
            logger.info("Deleted object '{}' from S3 bucket '{}'", objectName, s3Config.getBucketName());
        } catch (Exception e) {
            throw new StorageException("Failed to delete object from S3: " + objectName, e);
        }
    }

    /**
     * Checks whether an object exists in S3 using a lightweight HeadObject request.
     *
     * @param objectName the S3 object key (file hash)
     * @return {@code true} if the object exists, {@code false} otherwise
     */
    @Override
    public boolean objectExists(String objectName) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectName)
                    .build();

            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            logger.warn("Unexpected error while checking existence of object '{}': {}", objectName, e.getMessage());
            return false;
        }
    }

    /**
     * Generates the standard public S3 URL for an object.
     * Note: this URL is only accessible if the object or bucket has public-read ACL.
     *
     * @param objectName the S3 object key (file hash)
     * @return fully qualified S3 URL
     */
    @Override
    public String generateObjectUrl(String objectName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                s3Config.getBucketName(), s3Config.getRegion(), objectName);
    }
}
