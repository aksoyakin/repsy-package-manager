package io.repsy.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<PackageEntity, PackageEntity.PackageId> {

    Optional<PackageEntity> findByPackageNameAndVersion(String packageName, String version);

    boolean existsByPackageNameAndVersion(String packageName, String version);
}
