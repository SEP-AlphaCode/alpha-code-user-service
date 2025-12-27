package com.alpha_code.alpha_code_user_service.service;

import com.alpha_code.alpha_code_user_service.service.impl.S3ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private Region awsRegion;

    @InjectMocks
    private S3ServiceImpl s3Service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucketName", "test-bucket");
        when(awsRegion.id()).thenReturn("us-east-1");
    }

    @Test
    void testUploadFile_Success() throws Exception {
        File file = File.createTempFile("test", ".png");
        file.deleteOnExit();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(mock(PutObjectResponse.class));

        String result = s3Service.uploadFile(file);

        assertNotNull(result);
        assertTrue(result.contains("test-bucket"));
        assertTrue(result.contains("qrcodes"));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testUploadBytes_Success() {
        byte[] data = "test data".getBytes();
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(mock(PutObjectResponse.class));

        String result = s3Service.uploadBytes(data, "test/key.jpg", "image/jpeg");

        assertNotNull(result);
        assertTrue(result.contains("test-bucket"));
        assertTrue(result.contains("test/key.jpg"));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}

