package io.repsy.application.services;

import io.repsy.application.ports.in.DownloadPackageUseCase;
import io.repsy.application.ports.out.LoadPackagePort;
import io.repsy.application.ports.out.StoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DownloadPackageService implements DownloadPackageUseCase {

    private final StoragePort storagePort;
    private final LoadPackagePort loadPackagePort;

    @Override
    public InputStream downloadFile(String packageName, String version, String fileName) {
        try {
            if (!loadPackagePort.packageExists(packageName, version)) {
                log.warn("Package {}/{} does not exist in the database", packageName, version);
                return null;
            }

            if (!isValidFileName(fileName)) {
                log.warn("Invalid file name: {}", fileName);
                return null;
            }

            if (!storagePort.fileExists(packageName, version, fileName)) {
                log.warn("File {}/{}/{} does not exist in the storage", packageName, version, fileName);
                return null;
            }

            return storagePort.retrieveFile(packageName, version, fileName);
        } catch (Exception e) {
            log.error("Failed to download file {}/{}/{}", packageName, version, fileName, e);
            return null;
        }
    }

    @Override
    public boolean fileExists(String packageName, String version, String fileName) {
        try {
            if (!loadPackagePort.packageExists(packageName, version)) {
                return false;
            }

            if (!isValidFileName(fileName)) {
                return false;
            }

            return storagePort.fileExists(packageName, version, fileName);
        } catch (Exception e) {
            log.error("Failed to check if file exists {}/{}/{}", packageName, version, fileName, e);
            return false;
        }
    }

    private boolean isValidFileName(String fileName) {
        return "package.rep".equals(fileName) || "meta.json".equals(fileName);
    }
}
