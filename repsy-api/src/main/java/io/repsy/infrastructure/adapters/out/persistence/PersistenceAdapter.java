package io.repsy.infrastructure.adapters.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.repsy.application.ports.out.LoadPackagePort;
import io.repsy.application.ports.out.SavePackagePort;
import io.repsy.domain.Package;
import io.repsy.domain.PackageMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PersistenceAdapter implements SavePackagePort, LoadPackagePort {

    private final PackageRepository packageRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<Package> loadPackage(String packageName, String version) {
        return packageRepository.findByPackageNameAndVersion(packageName, version)
                .map(this::mapToDomainPackage);
    }

    @Override
    public boolean packageExists(String packageName, String version) {
        return packageRepository.existsByPackageNameAndVersion(packageName, version);
    }

    @Override
    public boolean savePackage(Package packageData) {
        try {
            PackageEntity entity = mapToJpaEntity(packageData);
            packageRepository.save(entity);
            return true;
        } catch (Exception e) {
            log.error("Failed to save package: {}/{}", packageData.getName(), packageData.getVersion(), e);
            return false;
        }
    }

    @Override
    public boolean updatePackageMetadata(String packageName, String version, PackageMetadata metadata) {
        try {
            Optional<PackageEntity> optionalEntity = packageRepository.findByPackageNameAndVersion(packageName, version);
            if (optionalEntity.isEmpty()) {
                log.warn("Package not found: {}/{}", packageName, version);
                return false;
            }

            PackageEntity entity = optionalEntity.get();
            entity.setAuthor(metadata.getAuthor());
            entity.setMetadataJson(objectMapper.writeValueAsString(metadata));

            packageRepository.save(entity);
            return true;
        } catch (JsonProcessingException e) {
            log.error("Failed to convert metadata to JSON: {}/{}", packageName, version, e);
            return false;
        } catch (Exception e) {
            log.error("Failed to update package metadata: {}/{}", packageName, version, e);
            return false;
        }
    }

    @Override
    public boolean updatePackageFileStatus(String packageName, String version, boolean isPackageFile, boolean hasFile) {
        try {
            Optional<PackageEntity> optionalEntity = packageRepository.findByPackageNameAndVersion(packageName, version);
            if (optionalEntity.isEmpty()) {
                PackageEntity entity = PackageEntity.builder()
                        .packageName(packageName)
                        .version(version)
                        .hasPackageFile(isPackageFile ? hasFile : false)
                        .hasMetadataFile(!isPackageFile ? hasFile : false)
                        .createdAt(java.time.LocalDateTime.now())
                        .build();

                packageRepository.save(entity);
            } else {
                PackageEntity entity = optionalEntity.get();
                if (isPackageFile) {
                    entity.setHasPackageFile(hasFile);
                } else {
                    entity.setHasMetadataFile(hasFile);
                }

                packageRepository.save(entity);
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to update package file status: {}/{}", packageName, version, e);
            return false;
        }
    }

    private Package mapToDomainPackage(PackageEntity entity) {
        PackageMetadata metadata = null;
        if (entity.getMetadataJson() != null && !entity.getMetadataJson().isEmpty()) {
            try {
                metadata = objectMapper.readValue(entity.getMetadataJson(), PackageMetadata.class);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse metadata JSON for {}/{}", entity.getPackageName(), entity.getVersion(), e);
            }
        }

        return Package.builder()
                .name(entity.getPackageName())
                .version(entity.getVersion())
                .hasPackageFile(entity.isHasPackageFile())
                .hasMetadataFile(entity.isHasMetadataFile())
                .createdAt(entity.getCreatedAt())
                .metadata(metadata)
                .build();
    }

    private PackageEntity mapToJpaEntity(Package packageData) {
        String metadataJson = null;
        if (packageData.getMetadata() != null) {
            try {
                metadataJson = objectMapper.writeValueAsString(packageData.getMetadata());
            } catch (JsonProcessingException e) {
                log.error("Failed to convert metadata to JSON for {}/{}",
                        packageData.getName(), packageData.getVersion(), e);
            }
        }

        return PackageEntity.builder()
                .packageName(packageData.getName())
                .version(packageData.getVersion())
                .hasPackageFile(packageData.isHasPackageFile())
                .hasMetadataFile(packageData.isHasMetadataFile())
                .createdAt(packageData.getCreatedAt())
                .author(packageData.getMetadata() != null ? packageData.getMetadata().getAuthor() : null)
                .metadataJson(metadataJson)
                .build();
    }
}
