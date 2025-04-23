package io.repsy.application.ports.in;

import java.io.InputStream;

public interface DeployPackageUseCase {

    boolean deployPackageFile(String packageName, String version, InputStream packageContent);

    boolean deployMetadataFile(String packageName, String version, InputStream metadataContent);
}
