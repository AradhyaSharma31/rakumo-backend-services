package com.Rakumo.gateway.controller;

import com.Rakumo.gateway.grpc.*;
import com.Rakumo.gateway.service.GrpcObjectClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

@RestController
@RequestMapping("/api/objects")
@RequiredArgsConstructor
public class ObjectGatewayController {

    private final GrpcObjectClientService objectClientService;

    // ========== FILE STORAGE ENDPOINTS ==========

    @PostMapping(value = "/store", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StoreFileResponseMessage> storeFile(@RequestBody StoreFileRequestMessage request) {
        StoreFileResponseMessage response = objectClientService.storeFile(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download-stream/{bucketName}/{objectKey}")
    public ResponseEntity<StreamingResponseBody> downloadFileStream(
            @PathVariable String bucketName,
            @PathVariable String objectKey,
            @RequestParam(required = false) String versionId) {

        RetrieveFileRequestMessage request = RetrieveFileRequestMessage.newBuilder()
                .setBucketName(bucketName)
                .setObjectKey(objectKey)
                .setVersionId(versionId != null ? versionId : "")
                .build();

        Iterator<FileChunkMessage> chunkIterator = objectClientService.retrieveFileStream(request);

        StreamingResponseBody streamingBody = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                try {
                    while (chunkIterator.hasNext()) {
                        FileChunkMessage chunk = chunkIterator.next();
                        outputStream.write(chunk.getData().toByteArray());
                        outputStream.flush();
                    }
                } catch (Exception e) {
                    throw new IOException("Streaming failed", e);
                }
            }
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", objectKey);

        return new ResponseEntity<>(streamingBody, headers, HttpStatus.OK);
    }

    @DeleteMapping("/{bucketName}/{objectKey}")
    public ResponseEntity<DeleteFileResponseMessage> deleteFile(
            @PathVariable String bucketName,
            @PathVariable String objectKey,
            @RequestParam(required = false) String versionId) {

        DeleteFileRequestMessage request = DeleteFileRequestMessage.newBuilder()
                .setBucketName(bucketName)
                .setObjectKey(objectKey)
                .setVersionId(versionId != null ? versionId : "")
                .build();

        DeleteFileResponseMessage response = objectClientService.deleteFile(request);
        return ResponseEntity.ok(response);
    }

    // ========== UPLOAD ENDPOINTS ==========

    @PostMapping(value = "/upload", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<UploadResponseMessage> uploadFile(@RequestBody UploadFileRequestMessage request) {
        UploadResponseMessage response = objectClientService.handleRegularUpload(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/multipart/initiate")
    public ResponseEntity<InitiateMultipartResponseMessage> initiateMultipartUpload(
            @RequestBody InitiateMultipartRequestMessage request) {
        InitiateMultipartResponseMessage response = objectClientService.initiateMultipartUpload(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/multipart/upload-chunk", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<UploadChunkResponseMessage> uploadChunk(@RequestBody UploadChunkRequestMessage request) {
        UploadChunkResponseMessage response = objectClientService.uploadChunk(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/multipart/complete")
    public ResponseEntity<UploadResponseMessage> completeMultipartUpload(
            @RequestBody CompleteMultipartRequestMessage request) {
        UploadResponseMessage response = objectClientService.completeMultipartUpload(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/multipart/abort")
    public ResponseEntity<AbortMultipartResponseMessage> abortMultipartUpload(
            @RequestBody AbortMultipartRequestMessage request) {
        AbortMultipartResponseMessage response = objectClientService.abortMultipartUpload(request);
        return ResponseEntity.ok(response);
    }

    // ========== DOWNLOAD ENDPOINTS ==========

    @GetMapping("/download/{bucketName}/{objectKey}")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable String bucketName,
            @PathVariable String objectKey,
            @RequestParam(required = false) String versionId) {

        DownloadRequestMessage request = DownloadRequestMessage.newBuilder()
                .setBucketName(bucketName)
                .setObjectKey(objectKey)
                .setVersionId(versionId != null ? versionId : "")
                .build();

        DownloadResponseMessage response = objectClientService.retrieveFile(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(response.getContentType()));
        headers.setContentLength(response.getContentLength());
        headers.setContentDispositionFormData("attachment", objectKey);
        headers.set("ETag", response.getChecksum());
        headers.set("Last-Modified", response.getLastModified().toString());

        return new ResponseEntity<>(response.getFileData().toByteArray(), headers, HttpStatus.OK);
    }

    // ========== HEALTH CHECK ENDPOINTS ==========

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Object Gateway is healthy");
    }
}