package io.repsy.application.ports.out;

import io.repsy.domain.Package;

import java.util.Optional;

public interface LoadPackagePort {

    Optional<Package> loadPackage(String packageName, String version);

    boolean packageExists(String packageName, String version);
}
