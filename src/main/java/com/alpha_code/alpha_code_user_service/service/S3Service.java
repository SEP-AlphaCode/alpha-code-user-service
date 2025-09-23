package com.alpha_code.alpha_code_user_service.service;

import java.io.File;

public interface S3Service {
    String uploadFile(File file);

    String uploadBytes(byte[] data, String key, String contentType);
}
