package io.repsy.storage;

import java.io.InputStream;

public interface StorageStrategy {

    boolean storeFile(String packageName, String version, String fileName, InputStream content);

    InputStream retrieveFile(String packageName, String version, String fileName);

    boolean fileExists(String packageName, String version, String fileName);
}