//package com.Rakumo.metadata;
//
//import com.Rakumo.metadata.DTO.BucketDTO;
//import com.Rakumo.metadata.Exceptions.BucketNotFoundException;
//import com.Rakumo.metadata.Services.BucketService;
//import com.Rakumo.metadata.gRPC.BucketGrpcService;
//import com.google.protobuf.Timestamp;
//import com.rakumo.metadata.bucket.*;
//import io.grpc.Status;
//import io.grpc.StatusRuntimeException;
//import io.grpc.stub.StreamObserver;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.Instant;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class BucketGrpcServiceTest {
//
//    @Mock
//    private BucketService bucketService;
//
//    @Mock
//    private StreamObserver<BucketResponse> bucketResponseObserver;
//
//    @Mock
//    private StreamObserver<DeleteResponse> deleteResponseObserver;
//
//    @InjectMocks
//    private BucketGrpcService bucketGrpcService;
//
//    private final UUID testOwnerId = UUID.randomUUID();
//    private final UUID testBucketId = UUID.randomUUID();
//    private final Instant testInstant = Instant.now();
//
//    @BeforeEach
//    void setUp() {
//        // Common setup if needed
//    }
//
//    @Test
//    void createBucket_Success() {
//        // Arrange
//        CreateBucketRequest request = CreateBucketRequest.newBuilder()
//                .setOwnerId(testOwnerId.toString())
//                .setName("test-bucket")
//                .setVersioningEnabled(true)
//                .setRegion("us-east-1")
//                .build();
//
//        BucketDTO mockDto = new BucketDTO(
//                testBucketId,
//                testOwnerId,
//                "test-bucket",
//                testInstant,
//                testInstant,
//                true,
//                "us-east-1"
//        );
//
//        when(bucketService.createBucket(any(UUID.class), anyString(), anyBoolean(), anyString()))
//                .thenReturn(mockDto);
//
//        // Act
//        bucketGrpcService.createBucket(request, bucketResponseObserver);
//
//        // Assert
//        ArgumentCaptor<BucketResponse> responseCaptor = ArgumentCaptor.forClass(BucketResponse.class);
//        verify(bucketResponseObserver).onNext(responseCaptor.capture());
//        verify(bucketResponseObserver).onCompleted();
//
//        BucketResponse response = responseCaptor.getValue();
//        assertEquals(testBucketId.toString(), response.getBucketId());
//        assertEquals(testOwnerId.toString(), response.getOwnerId());
//        assertEquals("test-bucket", response.getName());
//        assertTrue(response.getVersioningEnabled());
//    }
//
//    @Test
//    void createBucket_Failure() {
//        // Arrange
//        CreateBucketRequest request = CreateBucketRequest.newBuilder()
//                .setOwnerId(testOwnerId.toString())
//                .setName("test-bucket")
//                .setVersioningEnabled(true)
//                .setRegion("us-east-1")
//                .build();
//
//        when(bucketService.createBucket(any(UUID.class), anyString(), anyBoolean(), anyString()))
//                .thenThrow(new RuntimeException("Database error"));
//
//        // Act
//        bucketGrpcService.createBucket(request, bucketResponseObserver);
//
//        // Assert
//        ArgumentCaptor<Throwable> throwableCaptor = ArgumentCaptor.forClass(Throwable.class);
//        verify(bucketResponseObserver).onError(throwableCaptor.capture());
//
//        Throwable thrown = throwableCaptor.getValue();
//        assertInstanceOf(StatusRuntimeException.class, thrown);
//        assertEquals(Status.INTERNAL.getCode(), ((StatusRuntimeException) thrown).getStatus().getCode());
//        assertNotNull(((StatusRuntimeException) thrown).getStatus().getDescription());
//        assertTrue(((StatusRuntimeException) thrown).getStatus().getDescription().contains("Database error"));
//    }
//
//    @Test
//    void getBucket_Success() throws BucketNotFoundException {
//        // Arrange
//        GetBucketRequest request = GetBucketRequest.newBuilder()
//                .setOwnerId(testOwnerId.toString())
//                .setBucketId(testBucketId.toString())
//                .build();
//
//        BucketDTO mockDto = new BucketDTO(
//                testBucketId,
//                testOwnerId,
//                "test-bucket",
//                testInstant,
//                testInstant,
//                false,
//                "us-east-1"
//        );
//
//        when(bucketService.getBucket(testOwnerId, testBucketId)).thenReturn(mockDto);
//
//        // Act
//        bucketGrpcService.getBucket(request, bucketResponseObserver);
//
//        // Assert
//        ArgumentCaptor<BucketResponse> responseCaptor = ArgumentCaptor.forClass(BucketResponse.class);
//        verify(bucketResponseObserver).onNext(responseCaptor.capture());
//        verify(bucketResponseObserver).onCompleted();
//
//        BucketResponse response = responseCaptor.getValue();
//        assertEquals(testBucketId.toString(), response.getBucketId());
//        assertEquals(testOwnerId.toString(), response.getOwnerId());
//        assertEquals("test-bucket", response.getName());
//        assertFalse(response.getVersioningEnabled());
//    }
//
//    @Test
//    void getBucket_NotFound() throws BucketNotFoundException {
//        // Arrange
//        GetBucketRequest request = GetBucketRequest.newBuilder()
//                .setOwnerId(testOwnerId.toString())
//                .setBucketId(testBucketId.toString())
//                .build();
//
//        when(bucketService.getBucket(testOwnerId, testBucketId))
//                .thenThrow(new BucketNotFoundException("Bucket not found"));
//
//        // Act
//        bucketGrpcService.getBucket(request, bucketResponseObserver);
//
//        // Assert
//        ArgumentCaptor<Throwable> throwableCaptor = ArgumentCaptor.forClass(Throwable.class);
//        verify(bucketResponseObserver).onError(throwableCaptor.capture());
//
//        Throwable thrown = throwableCaptor.getValue();
//        assertInstanceOf(StatusRuntimeException.class, thrown);
//        assertEquals(Status.NOT_FOUND.getCode(), ((StatusRuntimeException) thrown).getStatus().getCode());
//        assertEquals("Bucket not found", ((StatusRuntimeException) thrown).getStatus().getDescription());
//    }
//
//    @Test
//    void updateBucket_Success() {
//        // Arrange
//        UpdateBucketRequest request = UpdateBucketRequest.newBuilder()
//                .setBucketId(testBucketId.toString())
//                .setName("updated-bucket")
//                .setVersioningEnabled(true)
//                .setRegion("eu-west-1")
//                .build();
//
//        BucketDTO mockDto = new BucketDTO(
//                testBucketId,
//                testOwnerId,
//                "updated-bucket",
//                testInstant,
//                testInstant,
//                true,
//                "eu-west-1"
//        );
//
//        when(bucketService.updateBucket(testBucketId, "updated-bucket", true, "eu-west-1"))
//                .thenReturn(mockDto);
//
//        // Act
//        bucketGrpcService.updateBucket(request, bucketResponseObserver);
//
//        // Assert
//        ArgumentCaptor<BucketResponse> responseCaptor = ArgumentCaptor.forClass(BucketResponse.class);
//        verify(bucketResponseObserver).onNext(responseCaptor.capture());
//        verify(bucketResponseObserver).onCompleted();
//
//        BucketResponse response = responseCaptor.getValue();
//        assertEquals(testBucketId.toString(), response.getBucketId());
//        assertEquals("updated-bucket", response.getName());
//        assertTrue(response.getVersioningEnabled());
//        assertEquals("eu-west-1", mockDto.getRegion());
//    }
//
//    @Test
//    void updateBucket_Failure() {
//        // Arrange
//        UpdateBucketRequest request = UpdateBucketRequest.newBuilder()
//                .setBucketId(testBucketId.toString())
//                .setName("updated-bucket")
//                .setVersioningEnabled(true)
//                .setRegion("eu-west-1")
//                .build();
//
//        when(bucketService.updateBucket(testBucketId, "updated-bucket", true, "eu-west-1"))
//                .thenThrow(new RuntimeException("Update failed"));
//
//        // Act
//        bucketGrpcService.updateBucket(request, bucketResponseObserver);
//
//        // Assert
//        ArgumentCaptor<Throwable> throwableCaptor = ArgumentCaptor.forClass(Throwable.class);
//        verify(bucketResponseObserver).onError(throwableCaptor.capture());
//
//        Throwable thrown = throwableCaptor.getValue();
//        assertInstanceOf(StatusRuntimeException.class, thrown);
//        assertEquals(Status.INTERNAL.getCode(), ((StatusRuntimeException) thrown).getStatus().getCode());
//        assertTrue(((StatusRuntimeException) thrown).getStatus().getDescription().contains("Update failed"));
//    }
//
////    @Test
////    void deleteBucket_Success() {
////        // Arrange
////        DeleteBucketRequest request = DeleteBucketRequest.newBuilder()
////                .setBucketId(testBucketId.toString())
////                .setOwnerId(testOwnerId.toString())
////                .build();
////
////        // No return value expected for void method
////        doNothing().when(bucketService).deleteBucket(testBucketId, testOwnerId);
////
////        // Act
////        bucketGrpcService.deleteBucket(request, deleteResponseObserver);
////
////        // Assert
////        ArgumentCaptor<DeleteResponse> responseCaptor = ArgumentCaptor.forClass(DeleteResponse.class);
////        verify(deleteResponseObserver).onNext(responseCaptor.capture());
////        verify(deleteResponseObserver).onCompleted();
////
////        DeleteResponse response = responseCaptor.getValue();
////        assertTrue(response.getSuccess());
////        assertEquals("Bucket deleted successfully", response.getMessage());
////    }
////
////    @Test
////    void deleteBucket_NotFound() {
////        // Arrange
////        DeleteBucketRequest request = DeleteBucketRequest.newBuilder()
////                .setBucketId(testBucketId.toString())
////                .setOwnerId(testOwnerId.toString())
////                .build();
////
////        doThrow(new BucketNotFoundException("Bucket not found"))
////                .when(bucketService).deleteBucket(testBucketId, testOwnerId);
////
////        // Act
////        bucketGrpcService.deleteBucket(request, deleteResponseObserver);
////
////        // Assert
////        ArgumentCaptor<DeleteResponse> responseCaptor = ArgumentCaptor.forClass(DeleteResponse.class);
////        verify(deleteResponseObserver).onNext(responseCaptor.capture());
////        verify(deleteResponseObserver).onCompleted();
////
////        DeleteResponse response = responseCaptor.getValue();
////        assertFalse(response.getSuccess());
////        assertEquals("Bucket not found", response.getMessage());
////    }
//
////    @Test
////    void deleteBucket_Failure() {
////        // Arrange
////        DeleteBucketRequest request = DeleteBucketRequest.newBuilder()
////                .setBucketId(testBucketId.toString())
////                .setOwnerId(testOwnerId.toString())
////                .build();
////
////        doThrow(new RuntimeException("Delete failed"))
////                .when(bucketService).deleteBucket(testBucketId, testOwnerId);
////
////        // Act
////        bucketGrpcService.deleteBucket(request, deleteResponseObserver);
////
////        // Assert
////        ArgumentCaptor<Throwable> throwableCaptor = ArgumentCaptor.forClass(Throwable.class);
////        verify(deleteResponseObserver).onError(throwableCaptor.capture());
////
////        Throwable thrown = throwableCaptor.getValue();
////        assertInstanceOf(StatusRuntimeException.class, thrown);
////        assertEquals(Status.INTERNAL.getCode(), ((StatusRuntimeException) thrown).getStatus().getCode());
////        assertTrue(((StatusRuntimeException) thrown).getStatus().getDescription().contains("Delete failed"));
////    }
//
//    @Test
//    void toBucketResponse_WithUpdatedAt() {
//        // Arrange
//        BucketDTO dto = new BucketDTO(
//                testBucketId,
//                testOwnerId,
//                "test-bucket",
//                testInstant,
//                testInstant,
//                true,
//                "us-east-1"
//        );
//
//        // Act
//        BucketResponse response = bucketGrpcService.toBucketResponse(dto);
//
//        // Assert
//        assertEquals(testBucketId.toString(), response.getBucketId());
//        assertEquals(testOwnerId.toString(), response.getOwnerId());
//        assertEquals("test-bucket", response.getName());
//        assertTrue(response.getVersioningEnabled());
//        assertNotNull(response.getCreatedAt());
//        assertNotNull(response.getUpdatedAt());
//    }
//
//    @Test
//    void toBucketResponse_WithoutUpdatedAt() {
//        // Arrange
//        BucketDTO dto = new BucketDTO(
//                testBucketId,
//                testOwnerId,
//                "test-bucket",
//                testInstant,
//                null,
//                true,
//                "us-east-1"
//        );
//
//        // Act
//        BucketResponse response = bucketGrpcService.toBucketResponse(dto);
//
//        // Assert
//        assertEquals(testBucketId.toString(), response.getBucketId());
//        assertEquals(testOwnerId.toString(), response.getOwnerId());
//        assertEquals("test-bucket", response.getName());
//        assertTrue(response.getVersioningEnabled());
//        assertNotNull(response.getCreatedAt());
//        assertEquals(Timestamp.getDefaultInstance(), response.getUpdatedAt());
//    }
//
//    @Test
//    void toTimestamp_Conversion() {
//        // Arrange
//        Instant now = Instant.now();
//
//        // Act
//        Timestamp timestamp = bucketGrpcService.toTimestamp(now);
//
//        // Assert
//        assertEquals(now.getEpochSecond(), timestamp.getSeconds());
//        assertEquals(now.getNano(), timestamp.getNanos());
//    }
//}