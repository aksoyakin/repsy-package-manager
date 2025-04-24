#!/bin/bash

echo "Building and deploying Repsy Storage Libraries"
echo "---------------------------------------------"

echo "Deploying parent POM to Repsy..."
mvn deploy -N
if [ $? -ne 0 ]; then
    echo "Failed to deploy parent POM. Aborting."
    exit 1
fi

echo ""
echo "Deploying File System Storage library to Repsy..."
cd storage-fs-lib
mvn clean deploy -DskipTests
if [ $? -ne 0 ]; then
    echo "Failed to deploy File System Storage library. Aborting."
    exit 1
fi
cd ..

echo ""
echo "Deploying Object Storage library to Repsy..."
cd storage-object-lib
mvn clean deploy -DskipTests
if [ $? -ne 0 ]; then
    echo "Failed to deploy Object Storage library. Aborting."
    exit 1
fi
cd ..

echo ""
echo "Libraries successfully deployed to Repsy Maven repository!"
echo "You can now build the main application using these libraries from Repsy."