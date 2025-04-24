package io.repsy.infrastructure.adapters.out.storage;

import io.repsy.application.ports.out.StoragePort;
import io.repsy.storage.StorageStrategy;
import io.repsy.storage.filesystem.FileSystemStorage;
import io.repsy.storage.object.ObjectStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@Slf4j
public class StorageAdapter implements StoragePort {

    private final StorageStrategy storageStrategy;

    public StorageAdapter(
            @Value("${storage.strategy}") String storageStrategyName,
            @Value("${storage.filesystem.base-path:./storage}") String baseStoragePath,
            @Value("${storage.object.endpoint:http://localhost:9000}") String minioEndpoint,
            @Value("${storage.object.access-key:minioadmin}") String minioAccessKey,
            @Value("${storage.object.secret-key:minioadmin}") String minioSecretKey,
            @Value("${storage.object.bucket-name:repsy}") String minioBucketName) {

        // Create the appropriate storage strategy based on configuration
        if ("file-system".equalsIgnoreCase(storageStrategyName)) {
            this.storageStrategy = new FileSystemStorage(baseStoragePath);
            log.info("Using File System Storage Strategy with base path: {}", baseStoragePath);
        } else if ("object-storage".equalsIgnoreCase(storageStrategyName)) {
            this.storageStrategy = new ObjectStorage(minioEndpoint, minioAccessKey, minioSecretKey, minioBucketName);
            log.info("Using Object Storage Strategy with endpoint: {}", minioEndpoint);
        } else {
            log.warn("Invalid storage strategy: {}. Using File System Storage as default.", storageStrategyName);
            this.storageStrategy = new FileSystemStorage(baseStoragePath);
        }
    }

    @Override
    public boolean storeFile(String packageName, String version, String fileName, InputStream content) {
        return storageStrategy.storeFile(packageName, version, fileName, content);
    }

    @Override
    public InputStream retrieveFile(String packageName, String version, String fileName) {
        return storageStrategy.retrieveFile(packageName, version, fileName);
    }

    @Override
    public boolean fileExists(String packageName, String version, String fileName) {
        return storageStrategy.fileExists(packageName, version, fileName);
    }
}