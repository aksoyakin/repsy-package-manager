
# Repsy Package Manager

A repository system for Repsy Assignment.

## Overview

Repsy Package Manager is a REST API that allows for deployment and downloading of Repsy packages. It provides the following features:

- Deployment of package files (package.rep) and metadata files (meta.json)
- Downloading of package files and metadata files
- Storage of files using either file system or object storage (MinIO)
- Metadata validation and storage


## Architecture

The application is built using a Clean/Hexagonal Architecture:

- **Domain Layer**: Contains the core business entities and logic
- **Application Layer**: Contains use cases and port definitions
- **Infrastructure Layer**: Contains adapters for external systems (database, storage, web)

## Storage Strategies

The application supports two storage strategies:

1. **File System Storage**: Stores files in the local file system
2. **Object Storage**: Stores files in a MinIO object storage

The storage strategy can be configured using the `storage.strategy` property in the `application.properties` file.

## API Endpoints

### Deployment

```
PUT /{packageName}/{version}
```

This endpoint accepts file uploads for package.rep and meta.json files. The file type is determined by the filename.

### Download

```
GET /{packageName}/{version}/{fileName}
```

This endpoint allows downloading of package.rep and meta.json files.

## Running the Application

### Prerequisites

- Docker and Docker Compose

### Run with Docker Compose

1. Clone the repository
2. Run the following command:

```bash
docker-compose up -d
```

This will start the following services:
- **Repsy API**: The main application (available at http://localhost:8080)
- **PostgreSQL**: The database
- **MinIO**: The object storage server (available at http://localhost:9001)

## Configuration

### Storage Strategy

You can switch between storage strategies by changing the `STORAGE_STRATEGY` environment variable in the `docker-compose.yml` file:

```yaml
environment:
  - STORAGE_STRATEGY=file-system  # Options: file-system, object-storage
```

### Database

The application uses PostgreSQL as the database. Connection settings can be configured in the `application.properties` file or via environment variables.

### Storage

#### File System Storage

File system storage settings can be configured in the `application.properties` file or via environment variables:

```properties
storage.filesystem.base-path=./storage
```

#### Object Storage (MinIO)

MinIO settings can be configured in the `application.properties` file or via environment variables:

```properties
storage.object.endpoint=http://minio:9000
storage.object.access-key=minioadmin
storage.object.secret-key=minioadmin
storage.object.bucket-name=repsy
```
