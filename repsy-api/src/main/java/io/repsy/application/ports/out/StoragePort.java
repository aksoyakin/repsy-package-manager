package io.repsy.application.ports.out;

import java.io.InputStream;

public interface StoragePort {

    boolean storeFile(String packageName, String version, String filename, InputStream content);

    InputStream retrieveFile(String packageName, String version, String filename);

    boolean fileExists(String packageName, String version, String fileName);
}
