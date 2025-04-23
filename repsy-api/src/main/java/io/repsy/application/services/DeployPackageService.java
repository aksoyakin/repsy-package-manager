package io.repsy.application.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.repsy.application.ports.in.DeployPackageUseCase;
import io.repsy.application.ports.out.LoadPackagePort;
import io.repsy.application.ports.out.SavePackagePort;
import io.repsy.application.ports.out.StoragePort;
import io.repsy.domain.Package;
import io.repsy.domain.PackageMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeployPackageService implements DeployPackageUseCase {

    private final StoragePort storagePort;
    private final SavePackagePort savePackagePort;
    private final LoadPackagePort loadPackagePort;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public boolean deployPackageFile(String packageName, String version, InputStream packageContent) {
        try {
            boolean stored = storagePort.storeFile(packageName, version, "package.rep", packageContent);
            if (!stored) {
                log.warn("Failed to store package.rep file for package: {}/{}", packageName, version);
                return false;
            }

            createOrUpdatePackage(packageName, version, true, false);
            return true;
        } catch (Exception e) {
            log.error("Failed to deploy package.rep file for package: {}/{}", packageName, version, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deployMetadataFile(String packageName, String version, InputStream metadataContent) {
        try {
            byte[] metadataBytes = metadataContent.readAllBytes();

            PackageMetadata metadata = parseAndValidateMetadata(new ByteArrayInputStream(metadataBytes),
                    packageName, version);
            if (metadata == null) {
                return false;
            }

            boolean stored = storagePort.storeFile(packageName, version, "meta.json",
                    new ByteArrayInputStream(metadataBytes));
            if (!stored) {
                log.error("Failed to store meta.json file for package: {}/{}", packageName, version);
                return false;
            }

            Package packageData = createOrUpdatePackage(packageName, version, false, true);

            savePackagePort.updatePackageMetadata(packageName, version, metadata);
            return true;
        } catch (Exception e) {
            log.error("Failed to deploy meta.json file for package: {}/{}", packageName, version, e);
            return false;
        }
    }


    private Package createOrUpdatePackage(String packageName, String version, boolean hasPackageFile, boolean hasMetadaFile) {
        Optional<Package> existingPackage = loadPackagePort.loadPackage(packageName, version);

        if (existingPackage.isPresent()) {
            Package packageData = existingPackage.get();

            if (hasPackageFile) {
                savePackagePort.updatePackageFileStatus(packageName, version, true, true);
                packageData.setHasPackageFile(true);
            }

            if (hasMetadaFile) {
                savePackagePort.updatePackageFileStatus(packageName, version, false, true);
                packageData.setHasPackageFile(true);
            }

            return packageData;
        } else {
            Package newPackage = Package.builder()
                    .name(packageName)
                    .version(version)
                    .hasPackageFile(hasPackageFile)
                    .hasMetadataFile(hasMetadaFile)
                    .createdAt(LocalDateTime.now())
                    .build();

            savePackagePort.savePackage(newPackage);
            return newPackage;
        }
    }

    private PackageMetadata parseAndValidateMetadata(InputStream metadataContent, String expectedPackageName, String expectedVersion) {
        try {
            PackageMetadata metadata = objectMapper.readValue(metadataContent, PackageMetadata.class);

            if (!metadata.getName().equals(expectedPackageName) || !metadata.getVersion().equals(expectedVersion)) {
                log.error("Metadata package name/version ({}/{}) does not match expected values ({}/{})",
                        metadata.getName(), metadata.getVersion(), expectedPackageName, expectedVersion);
                return null;
            }

            return metadata;
        } catch (IOException e) {
            log.error("Failed to parse metadata JSON for {}/{}", expectedPackageName, expectedVersion, e);
            return null;
        }
    }
}
