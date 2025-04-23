package io.repsy.infrastructure.adapters.in.web;

import io.repsy.application.ports.in.DeployPackageUseCase;
import io.repsy.application.ports.in.DownloadPackageUseCase;
import io.repsy.infrastructure.adapters.in.web.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Package Management", description = "APIs for deploying and downloading Repsy packages")
public class PackageController {

    private final DeployPackageUseCase deployPackageUseCase;
    private final DownloadPackageUseCase downloadPackageUseCase;

    @Operation(summary = "Deploy a package or metadata file",
            description = "Upload a package.rep or meta.json file for a specific package and version")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File deployed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file name or empty file"),
            @ApiResponse(responseCode = "500", description = "Failed to deploy file")
    })
    @PutMapping("/{packageName}/{version}")
    public ResponseEntity<ResponseDTO> deployPackage(
            @PathVariable String packageName,
            @PathVariable String version,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResponseDTO("File is empty"));
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !(fileName.equals("package.rep") || fileName.equals("meta.json"))) {
            return ResponseEntity.badRequest().body(
                    new ResponseDTO("Invalid file name. Only 'package.rep' or 'meta.json' are allowed"));
        }

        try {
            boolean success;
            if ("package.rep".equals(fileName)) {
                success = deployPackageUseCase.deployPackageFile(packageName, version, file.getInputStream());
            } else {
                success = deployPackageUseCase.deployMetadataFile(packageName, version, file.getInputStream());
            }

            if (success) {
                return ResponseEntity.ok(new ResponseDTO("File deployed successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ResponseDTO("Failed to deploy file"));
            }
        } catch (IOException e) {
            log.error("Failed to read file for {}/{}", packageName, version, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO("Failed to read file: " + e.getMessage()));
        }
    }

    @Operation(summary = "Download a package or metadata file",
            description = "Download a package.rep or meta.json file for a specific package and version")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file name"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "500", description = "Failed to retrieve file")
    })
    @GetMapping("/{packageName}/{version}/{fileName}")
    public ResponseEntity<?> downloadPackage(
            @PathVariable String packageName,
            @PathVariable String version,
            @PathVariable String fileName) {

        if (!(fileName.equals("package.rep") || fileName.equals("meta.json"))) {
            return ResponseEntity.badRequest().body(
                    new ResponseDTO("Invalid file name. Only 'package.rep' or 'meta.json' are allowed"));
        }

        if (!downloadPackageUseCase.fileExists(packageName, version, fileName)) {
            return ResponseEntity.notFound().build();
        }

        InputStream fileContent = downloadPackageUseCase.downloadFile(packageName, version, fileName);
        if (fileContent == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO("Failed to retrieve file"));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", fileName);

        if ("package.rep".equals(fileName)) {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        } else {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(fileContent));
    }
}
