package io.repsy.storage.filesystem;

import io.repsy.storage.StorageStrategy;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class FileSystemStorage implements StorageStrategy {

    private final String baseStoragePath;

    public FileSystemStorage(String baseStoragePath) {
        this.baseStoragePath = baseStoragePath;
        createBaseDirectoryIfNotExists();
    }

    @Override
    public boolean storeFile(String packageName, String version, String fileName, InputStream content) {
        Path packagePath = getPackagePath(packageName, version);

        try {
            Files.createDirectories(packagePath);

            Path filePath = packagePath.resolve(fileName);
            Files.copy(content, filePath);

            log.info("Successfully stored file: {}/{}/{}", packageName, version, fileName);
            return true;
        } catch (IOException e){
            log.error("Failed to store file: {}/{}/{}", packageName, version, fileName, e);
            return false;
        }
    }

    @Override
    public InputStream retrieveFile(String packageName, String version, String fileName){
        Path filePath = getFilePath(packageName, version, fileName);

        try {
            if (Files.exists(filePath)) {
                return new FileInputStream(filePath.toFile());
            } else {
                log.warn("File not found: {}/{}/{}", packageName, version, fileName);
                return null;
            }
        } catch (IOException e) {
            log.error("Failed to retrieve file: {}/{}/{}", packageName, version, fileName, e);
            return null;
        }
    }

    @Override
    public boolean fileExists(String packageName, String version, String fileName){
        Path filePath = getFilePath(packageName, version, fileName);
        return Files.exists(filePath);
    }

    private void createBaseDirectoryIfNotExists() {
        try {
            Files.createDirectories(Paths.get(baseStoragePath));
            log.info("Base storage directory created at: {}", baseStoragePath);
        } catch (IOException e) {
            log.error("Failed to create base storage directory at: {}", baseStoragePath, e);
            throw new RuntimeException("Failed to create base storage directory", e);
        }
    }

    private Path getPackagePath(String packageName, String version) {
        return Paths.get(baseStoragePath, packageName, version);
    }

    private Path getFilePath(String packageName, String version, String filename) {
        return getPackagePath(packageName, version).resolve(filename);
    }
}