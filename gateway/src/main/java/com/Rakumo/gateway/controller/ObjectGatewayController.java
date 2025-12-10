package com.Rakumo.gateway.controller;

import com.Rakumo.gateway.dto.ObjectDTO.*;
import com.Rakumo.gateway.mapper.GrpcMapper;
import com.Rakumo.gateway.service.GrpcObjectClientService;
import com.Rakumo.object.storage.*;
import com.Rakumo.object.download.*;
import com.Rakumo.object.upload.*;
import com.Rakumo.object.presigned.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

@RestController
@RequestMapping("/api/objects")
@RequiredArgsConstructor
public class ObjectGatewayController {

    private final GrpcObjectClientService objectClientService;
    private final GrpcMapper mapper;

    // ========== FILE STORAGE ENDPOINTS ==========

    @PostMapping(value = "/store", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StoreFileResponseDTO> storeFile(@RequestBody StoreFileRequestDTO requestDTO) {
        StoreFileRequestMessage grpcRequest = mapper.toGrpcStoreFile(requestDTO);
        StoreFileResponseMessage grpcResponse = objectClientService.storeFile(grpcRequest);
        StoreFileResponseDTO responseDTO = mapper.toDtoStoreFile(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/download-stream/{bucketName}/{objectKey}")
    public ResponseEntity<StreamingResponseBody> downloadFileStream(
            @PathVariable String bucketName,
            @PathVariable String objectKey,
            @RequestParam(required = false) String versionId) {

        RetrieveFileRequestDTO requestDTO = new RetrieveFileRequestDTO(bucketName, objectKey,
                versionId != null ? versionId : "");
        RetrieveFileRequestMessage grpcRequest = mapper.toGrpcRetrieveFile(requestDTO);

        Iterator<FileChunkMessage> chunkIterator = objectClientService.retrieveFileStream(grpcRequest);

        StreamingResponseBody streamingBody = outputStream -> {
            int chunkCount = 0;
            long totalBytes = 0;
            try {
                while (chunkIterator.hasNext()) {
                    FileChunkMessage chunk = chunkIterator.next();
                    if (chunk.getData() != null && chunk.getData().size() > 0) {
                        byte[] chunkData = chunk.getData().toByteArray();
                        outputStream.write(chunkData);
                        outputStream.flush();

                        chunkCount++;
                        totalBytes += chunkData.length;
                    }
                }
            } catch (Exception e) {
                throw new IOException("Streaming failed", e);
            }
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", objectKey);

        return ResponseEntity.ok()
                .headers(headers)
                .body(streamingBody);
    }

    @DeleteMapping("{ownerId}/{bucketName}/{objectKey}/{fileId}")
    public ResponseEntity<DeleteFileResponseDTO> deleteFile(
            @PathVariable String ownerId,
            @PathVariable String bucketName,
            @PathVariable String objectKey,
            @PathVariable String fileId
            ) {

        DeleteFileRequestDTO requestDTO = new DeleteFileRequestDTO(ownerId, bucketName, objectKey,
                fileId);
        DeleteFileRequestMessage grpcRequest = mapper.toGrpcDeleteFile(requestDTO);

        DeleteFileResponseMessage grpcResponse = objectClientService.deleteFile(grpcRequest);
        DeleteFileResponseDTO responseDTO = mapper.toDtoDeleteFile(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    // ========== UPLOAD ENDPOINTS ==========

    @PostMapping(value = "/upload", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadResponseDTO> uploadFile(@RequestBody UploadFileRequestDTO requestDTO) {
        UploadFileRequestMessage grpcRequest = mapper.toGrpcUploadFile(requestDTO);
        UploadResponseMessage grpcResponse = objectClientService.handleRegularUpload(grpcRequest);
        UploadResponseDTO responseDTO = mapper.toDtoUpload(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/multipart/initiate")
    public ResponseEntity<InitiateMultipartResponseDTO> initiateMultipartUpload(
            @RequestBody InitiateMultipartRequestDTO requestDTO) {
        InitiateMultipartRequestMessage grpcRequest = mapper.toGrpcInitiateMultipart(requestDTO);
        InitiateMultipartResponseMessage grpcResponse = objectClientService.initiateMultipartUpload(grpcRequest);
        InitiateMultipartResponseDTO responseDTO = mapper.toDtoInitiateMultipart(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping(value = "/multipart/upload-chunk", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadChunkResponseDTO> uploadChunk(@RequestBody UploadChunkRequestDTO requestDTO) {
        UploadChunkRequestMessage grpcRequest = mapper.toGrpcUploadChunk(requestDTO);
        UploadChunkResponseMessage grpcResponse = objectClientService.uploadChunk(grpcRequest);
        UploadChunkResponseDTO responseDTO = mapper.toDtoUploadChunk(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/multipart/complete")
    public ResponseEntity<UploadResponseDTO> completeMultipartUpload(
            @RequestBody CompleteMultipartRequestDTO requestDTO) {
        CompleteMultipartRequestMessage grpcRequest = mapper.toGrpcCompleteMultipart(requestDTO);
        UploadResponseMessage grpcResponse = objectClientService.completeMultipartUpload(grpcRequest);
        UploadResponseDTO responseDTO = mapper.toDtoUpload(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/multipart/abort")
    public ResponseEntity<AbortMultipartResponseDTO> abortMultipartUpload(
            @RequestBody AbortMultipartRequestDTO requestDTO) {
        AbortMultipartRequestMessage grpcRequest = mapper.toGrpcAbortMultipart(requestDTO);
        AbortMultipartResponseMessage grpcResponse = objectClientService.abortMultipartUpload(grpcRequest);
        AbortMultipartResponseDTO responseDTO = mapper.toDtoAbortMultipart(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    // ========== DOWNLOAD ENDPOINTS ==========

    @GetMapping("/download/{bucketName}/{objectKey}")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable String bucketName,
            @PathVariable String objectKey,
            @RequestParam(required = false) String versionId) {

        DownloadRequestDTO requestDTO = new DownloadRequestDTO(bucketName, objectKey,
                versionId != null ? versionId : "");
        DownloadRequestMessage grpcRequest = mapper.toGrpcDownload(requestDTO);

        DownloadResponseMessage grpcResponse = objectClientService.retrieveFile(grpcRequest);
        DownloadResponseDTO responseDTO = mapper.toDtoDownload(grpcResponse);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(responseDTO.contentType()));
        headers.setContentLength(responseDTO.contentLength());
        headers.setContentDispositionFormData("attachment", objectKey);
        headers.set("ETag", responseDTO.checksum());
        headers.set("Last-Modified", responseDTO.lastModified().toString());

        return new ResponseEntity<>(responseDTO.fileData(), headers, HttpStatus.OK);
    }

    // ========== PRE-SIGNED URL ENDPOINTS ==========

    @PostMapping("/presigned/generate")
    public ResponseEntity<GeneratePreSignedUrlResponseDTO> generatePreSignedUrl(
            @RequestBody GeneratePreSignedUrlRequestDTO requestDTO) {
        GeneratePreSignedUrlRequest grpcRequest = mapper.toGrpcGeneratePreSignedUrl(requestDTO);
        GeneratePreSignedUrlResponse grpcResponse = objectClientService.generatePreSignedUrl(grpcRequest);
        GeneratePreSignedUrlResponseDTO responseDTO = mapper.toDtoGeneratePreSignedUrl(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/presigned/validate")
    public ResponseEntity<ValidatePreSignedUrlResponseDTO> validatePreSignedUrl(
            @RequestBody ValidatePreSignedUrlRequestDTO requestDTO) {
        ValidatePreSignedUrlRequest grpcRequest = mapper.toGrpcValidatePreSignedUrl(requestDTO);
        ValidatePreSignedUrlResponse grpcResponse = objectClientService.validatePreSignedUrl(grpcRequest);
        ValidatePreSignedUrlResponseDTO responseDTO = mapper.toDtoValidatePreSignedUrl(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    // ========== CONVENIENCE PRE-SIGNED URL ENDPOINTS ==========

    @GetMapping("/presigned/download/{bucketName}/{objectKey}")
    public ResponseEntity<GeneratePreSignedUrlResponseDTO> generateDownloadUrl(
            @PathVariable String bucketName,
            @PathVariable String objectKey,
            @RequestParam(required = false) String versionId,
            @RequestParam(defaultValue = "3600") int expiresIn) {

        GeneratePreSignedUrlResponse grpcResponse = objectClientService.generateDownloadUrl(
                bucketName, objectKey, versionId, expiresIn);
        GeneratePreSignedUrlResponseDTO responseDTO = mapper.toDtoGeneratePreSignedUrl(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/presigned/upload/{bucketName}/{objectKey}")
    public ResponseEntity<GeneratePreSignedUrlResponseDTO> generateUploadUrl(
            @PathVariable String bucketName,
            @PathVariable String objectKey,
            @RequestParam(required = false) String contentType,
            @RequestParam(defaultValue = "3600") int expiresIn) {

        GeneratePreSignedUrlResponse grpcResponse = objectClientService.generateUploadUrl(
                bucketName, objectKey, contentType, expiresIn);
        GeneratePreSignedUrlResponseDTO responseDTO = mapper.toDtoGeneratePreSignedUrl(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/presigned/delete/{bucketName}/{objectKey}")
    public ResponseEntity<GeneratePreSignedUrlResponseDTO> generateDeleteUrl(
            @PathVariable String bucketName,
            @PathVariable String objectKey,
            @RequestParam(required = false) String versionId,
            @RequestParam(defaultValue = "3600") int expiresIn) {

        GeneratePreSignedUrlResponse grpcResponse = objectClientService.generateDeleteUrl(
                bucketName, objectKey, versionId, expiresIn);
        GeneratePreSignedUrlResponseDTO responseDTO = mapper.toDtoGeneratePreSignedUrl(grpcResponse);
        return ResponseEntity.ok(responseDTO);
    }

    // ========== PRE-SIGNED URL REDIRECT ENDPOINTS ==========

    @GetMapping("/presigned/redirect/download/{bucketName}/{objectKey}")
    public ResponseEntity<byte[]> handlePreSignedDownload(
            @PathVariable String bucketName,
            @PathVariable String objectKey,
            @RequestParam String token,
            @RequestParam long expires) {

        String url = String.format("/api/objects/presigned/redirect/download/%s/%s?token=%s&expires=%d",
                bucketName, objectKey, token, expires);

        boolean isValid = objectClientService.isValidPreSignedUrl(url, bucketName, objectKey);

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            DownloadRequestDTO requestDTO = new DownloadRequestDTO(bucketName, objectKey, "");
            DownloadRequestMessage grpcRequest = mapper.toGrpcDownload(requestDTO);
            DownloadResponseMessage grpcResponse = objectClientService.retrieveFile(grpcRequest);
            DownloadResponseDTO responseDTO = mapper.toDtoDownload(grpcResponse);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(responseDTO.contentType()));
            headers.setContentLength(responseDTO.contentLength());
            headers.setContentDispositionFormData("attachment", objectKey);
            headers.set("ETag", responseDTO.checksum());
            headers.set("Last-Modified", responseDTO.lastModified().toString());

            return new ResponseEntity<>(responseDTO.fileData(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/presigned/redirect/upload/{bucketName}/{objectKey}")
    public ResponseEntity<UploadResponseDTO> handlePreSignedUpload(
            @PathVariable String bucketName,
            @PathVariable String objectKey,
            @RequestParam String token,
            @RequestParam long expires,
            @RequestBody byte[] fileData) {

        String url = String.format("http://localhost:9093/api/objects/presigned/redirect/download/%s/%s?token=%s&expires=%d",
                bucketName, objectKey, token, expires);

        boolean isValid = objectClientService.isValidPreSignedUrl(url, bucketName, objectKey);

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileData)) {
            UploadFileRequestDTO requestDTO = new UploadFileRequestDTO(
                    bucketName,
                    objectKey,
                    null,
                    null,
                    fileData);

            UploadFileRequestMessage grpcRequest = mapper.toGrpcUploadFile(requestDTO);
            UploadResponseMessage grpcResponse = objectClientService.handleRegularUpload(grpcRequest);
            UploadResponseDTO responseDTO = mapper.toDtoUpload(grpcResponse);

            return ResponseEntity.ok(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== HEALTH CHECK ENDPOINTS ==========

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Object Gateway is healthy");
    }
}