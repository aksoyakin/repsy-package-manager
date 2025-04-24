package io.repsy.infrastructure.adapters.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "packages")
@IdClass(PackageEntity.PackageId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageEntity {

    @Id
    @Column(name = "package_name", nullable = false)
    private String packageName;

    @Id
    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "has_package_file", nullable = false)
    private boolean hasPackageFile;

    @Column(name = "has_metadata_file", nullable = false)
    private boolean hasMetadataFile;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "author")
    private String author;

    @Lob
    @Column(name = "metadata_json")
    private String metadataJson;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PackageId implements Serializable {
        private String packageName;
        private String version;
    }
}