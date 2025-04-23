package io.repsy.application.ports.out;

import io.repsy.domain.Package;
import io.repsy.domain.PackageMetadata;

public interface SavePackagePort {

    boolean savePackage(Package packageData);

    boolean updatePackageMetadata(String packageName, String version, PackageMetadata metadata);

    boolean updatePackageFileStatus(String packageName, String version, boolean isPackageFile, boolean hasFile);
}
