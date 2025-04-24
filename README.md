
# Repsy Package Manager

A package management system for Repsy.

## Overview

Repsy Package Manager is a REST API that allows for deployment and downloading of Repsy packages. It provides the following features:

- Deployment of package files (package.rep) and metadata files (meta.json)
- Downloading of package files and metadata files
- Storage of files using either file system or object storage (MinIO)
- Metadata validation and storage

## System Requirements

- Docker and Docker Compose
- Java 21 (for local development)
- Maven 3.9+ (for local development)

## Architecture

The application is built using a Clean/Hexagonal Architecture:

- **Domain Layer**: Contains the core business entities and logic
- **Application Layer**: Contains use cases and port definitions
- **Infrastructure Layer**: Contains adapters for external systems (database, storage, web)

## Storage Strategies

The application supports two storage strategies:

1. **File System Storage**: Stores files in the local file system
2. **Object Storage**: Stores files in a MinIO object storage

## Getting Started

### Running with Docker Compose

1. Clone the repository:
   ```bash
   git clone https://github.com/aksoyakin/repsy-package-manager.git
   cd repsy-package-manager
   ```

2. Run the deployment script:
   ```bash
   chmod +x deploy-libraries.sh
   ./deploy-libraries.sh
   ```
3. Start the application and all dependencies using Docker Compose:
   ```bash
   docker compose up -d
   ```

   This will start:
    - Repsy API server on port 8080
    - PostgreSQL database on port 5433
    - MinIO object storage on port 9000 (API) and 9001 (Console)

4. Check if all services are running:
   ```bash
   docker compose ps
   ```

### Connecting to the Database

You can connect to the PostgreSQL database using any PostgreSQL client with the following credentials:

- **Host**: localhost
- **Port**: 5433 (mapped from 5432 inside the container)
- **Database**: repsy
- **Username**: postgres
- **Password**: 2546

Example using psql:

```bash
psql -h localhost -p 5433 -U postgres -d repsy
```

You can also use GUI tools like pgAdmin, DBeaver, or DataGrip with the same connection details.

## API Documentation

After starting the application, you can access the Swagger UI documentation at:
```
http://localhost:8080/swagger-ui.html
```

## Creating Package Files

### Creating a Package File (package.rep)

The `.rep` file is a binary package file that contains your software or library. You can create a simple package file using the following methods:

```bash
echo "This is a test package content" > package.rep
```

### Creating a Metadata File (meta.json)

The `meta.json` file contains metadata about your package. Based on the provided domain model, here's the proper format:

```json
{
  "name": "my-package",
  "version": "1.0.0",
  "author": "Your Name",
  "dependencies": [
    {
      "packageName": "other-package",
      "version": "2.0.0"
    },
    {
      "packageName": "another-dependency",
      "version": "1.5.2"
    }
  ]
}
```

You can create this file using any text editor or generate it programmatically:

```bash
echo '{
  "name": "my-package",
  "version": "1.0.0",
  "author": "Your Name",
  "dependencies": [
    {
      "packageName": "other-package",
      "version": "2.0.0"
    }
  ]
}' > meta.json
```

### Deploy a Package

To deploy a package file:

```bash
curl -X PUT http://localhost:8080/api/v1/{packageName}/{version} \
  -F "file=@/path/to/package.rep" \
  -H "Content-Type: multipart/form-data"
```

Example:
```bash
curl -X PUT http://localhost:8080/api/v1/my-package/1.0.0 \
  -F "file=@./package.rep" \
  -H "Content-Type: multipart/form-data"
```

### Deploy Metadata

To deploy metadata for a package:

```bash
curl -X PUT http://localhost:8080/api/v1/{packageName}/{version} \
  -F "file=@/path/to/meta.json" \
  -H "Content-Type: multipart/form-data"
```

Example:
```bash
curl -X PUT http://localhost:8080/api/v1/my-package/1.0.0 \
  -F "file=@./meta.json" \
  -H "Content-Type: multipart/form-data"
```

### Download a Package

To download a package file:

```bash
curl -OJ http://localhost:8080/api/v1/{packageName}/{version}/package.rep
```

Example:
```bash
curl -OJ http://localhost:8080/api/v1/my-package/1.0.0/package.rep
```

### Download Metadata

To download package metadata:

```bash
curl -OJ http://localhost:8080/api/v1/{packageName}/{version}/meta.json
```

Example:
```bash
curl -OJ http://localhost:8080/api/v1/my-package/1.0.0/meta.json
```

## Configuration

The application can be configured through the following environment variables:

| Environment Variable | Description | Default Value |
|----------------------|-------------|---------------|
| SPRING_DATASOURCE_URL | JDBC URL for the database | jdbc:postgresql://postgres:5432/repsy |
| SPRING_DATASOURCE_USERNAME | Database username | postgres |
| SPRING_DATASOURCE_PASSWORD | Database password | 2546 |
| STORAGE_STRATEGY | Storage strategy (file-system or object-storage) | file-system |
| STORAGE_FILESYSTEM_BASE_PATH | Base path for file system storage | /app/storage |
| STORAGE_OBJECT_ENDPOINT | MinIO endpoint URL | http://minio:9000 |
| STORAGE_OBJECT_ACCESS_KEY | MinIO access key | minioadmin |
| STORAGE_OBJECT_SECRET_KEY | MinIO secret key | minioadmin |
| STORAGE_OBJECT_BUCKET_NAME | MinIO bucket name | repsy |

You can modify these values in the `docker-compose.yml` file or pass them as environment variables when starting the containers.

## MinIO Object Storage

If you choose to use object storage, the MinIO console is available at:
```
http://localhost:9001
```

Login credentials:
- **Username**: minioadmin
- **Password**: minioadmin

From the console, you can manage buckets and view stored objects.
