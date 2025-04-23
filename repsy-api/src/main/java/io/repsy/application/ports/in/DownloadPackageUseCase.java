package io.repsy.application.ports.in;

import java.io.InputStream;

public interface DownloadPackageUseCase {

    InputStream downloadFile(String packageName, String version, String filename);

    boolean fileExists(String packageName, String version, String filename);
}
