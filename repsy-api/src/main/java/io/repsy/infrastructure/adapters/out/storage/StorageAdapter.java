package io.repsy.infrastructure.adapters.out.storage;

import io.repsy.application.ports.out.StoragePort;
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
    private final FileSystemStorage fileSystemStorage;
    private final ObjectStorage objectStorage;

    public StorageAdapter(
            @Value("${storage.strategy}") String storageStrategy,
            @Value("${storage.filesystem.base-path:./storage}") String baseStoragePath,
            @Value("${storage.object.endpoint:http://localhost:9000}") String minioEndpoint,
            @Value("${storage.object.access-key:minioadmin}") String minioAccessKey,
            @Value("${storage.object.secret-key:minioadmin}") String minioSecretKey,
            @Value("${storage.object.bucket-name:repsy}") String minioBucketName) {

        this.storageStrategy = StorageStrategy.fromString(storageStrategy);
        this.fileSystemStorage = new FileSystemStorage(baseStoragePath);
        this.objectStorage = new ObjectStorage(minioEndpoint, minioAccessKey, minioSecretKey, minioBucketName);

        log.info("Using storage strategy: {}", this.storageStrategy);
    }

    @Override
    public boolean storeFile(String packageName, String version, String fileName, InputStream content) {
        if (storageStrategy == StorageStrategy.FILE_SYSTEM) {
            return fileSystemStorage.storeFile(packageName, version, fileName, content);
        } else {
            return objectStorage.storeFile(packageName, version, fileName, content);
        }
    }

    @Override
    public InputStream retrieveFile(String packageName, String version, String fileName) {
        if (storageStrategy == StorageStrategy.FILE_SYSTEM) {
            return fileSystemStorage.retrieveFile(packageName, version, fileName);
        } else {
            return objectStorage.retrieveFile(packageName, version, fileName);
        }
    }

    @Override
    public boolean fileExists(String packageName, String version, String fileName) {
        if (storageStrategy == StorageStrategy.FILE_SYSTEM) {
            return fileSystemStorage.fileExists(packageName, version, fileName);
        } else {
            return objectStorage.fileExists(packageName, version, fileName);
        }
    }

    private enum StorageStrategy {
        FILE_SYSTEM,
        OBJECT_STORAGE;

        public static StorageStrategy fromString(String strategy) {
            if ("file-system".equalsIgnoreCase(strategy)) {
                return FILE_SYSTEM;
            } else if ("object-storage".equalsIgnoreCase(strategy)) {
                return OBJECT_STORAGE;
            } else {
                throw new IllegalArgumentException("Invalid storage strategy: " + strategy);
            }
        }
    }
}
