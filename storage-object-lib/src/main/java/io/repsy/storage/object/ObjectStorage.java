package io.repsy.storage.object;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.repsy.storage.StorageStrategy;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

@Slf4j
public class ObjectStorage implements StorageStrategy {

    private final MinioClient minioClient;
    private final String bucketName;

    public ObjectStorage(String endpoint, String accessKey, String secretKey, String bucketName) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.bucketName = bucketName;
        initializeBucket();
    }

    @Override
    public boolean storeFile(String packageName, String version, String fileName, InputStream content) {
        String objectName = getObjectName(packageName, version, fileName);

        try {
            long size = content.available();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(content, size, -1)
                            .build());

            log.info("Successfully stored file in object storage: {}/{}/{}", packageName, version, fileName);
            return true;
        } catch (Exception e) {
            log.error("Failed to store file in object storage: {}/{}/{}", packageName, version, fileName, e);
            return false;
        }
    }

    @Override
    public InputStream retrieveFile(String packageName, String version, String fileName) {
        String objectName = getObjectName(packageName, version, fileName);

        try {
            if (!fileExists(packageName, version, fileName)) {
                log.warn("File not found in object storage: {}/{}/{}", packageName, version, fileName);
                return null;
            }

            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e){
            log.error("Failed to retrieve file from object storage: {}/{}/{}", packageName, version, fileName, e);
            return null;
        }
    }

    @Override
    public boolean fileExists(String packageName, String version, String fileName) {
        String objectName = getObjectName(packageName, version, fileName);

        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {
            log.error("Failed to check if file exists in object storage: {}/{}/{}", packageName, version, fileName, e);
            return false;
        }
    }

    private void initializeBucket() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build());

            if (!bucketExists){
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build());
                log.info("Bucket created: {}", bucketName);
            } else {
                log.info("Bucket already exists: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize bucket: {}", bucketName, e);
            throw new RuntimeException("Failed to initialize bucket", e);
        }
    }

    private String getObjectName(String packageName, String version, String fileName) {
        return packageName + "/" + version + "/" + fileName;
    }
}