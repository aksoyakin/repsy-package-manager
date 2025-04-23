package io.repsy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Package {
    private String name;
    private String version;
    private boolean hasPackageFile;
    private boolean hasMetadataFile;
    private LocalDateTime createdAt;
    private PackageMetadata metadata;

    public boolean isComplete() {
        return hasPackageFile && hasMetadataFile;
    }
}
