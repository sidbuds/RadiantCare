package com.xixin.health.report.service;

import com.xixin.health.common.enums.ErrorCode;
import com.xixin.health.common.exception.BizException;
import com.xixin.health.report.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;

@Slf4j
@Service
public class ReportPdfStorageService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public ReportPdfStorageService(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    public void upload(String objectKey, byte[] content, String contentType, String fileName) {
        try {
            ensureBucket();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .stream(new ByteArrayInputStream(content), content.length, -1)
                    .contentType(contentType)
                    .headers(Collections.singletonMap("Content-Disposition", "attachment; filename=\"" + fileName + "\""))
                    .build());
        } catch (Exception e) {
            log.error("PDF upload failed: endpoint={}, bucket={}, objectKey={}",
                    properties.getEndpoint(), properties.getBucket(), objectKey, e);
            throw new BizException(ErrorCode.OPERATION_CONFLICT.getCode(), "PDF上传失败，请稍后重试");
        }
    }

    public StoredObject download(String objectKey) {
        try {
            GetObjectResponse response = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .build());
            return new StoredObject(response, parseContentLength(response.headers().get("Content-Length")), "application/pdf");
        } catch (Exception e) {
            log.error("PDF download failed: endpoint={}, bucket={}, objectKey={}",
                    properties.getEndpoint(), properties.getBucket(), objectKey, e);
            throw new BizException(ErrorCode.DATA_NOT_FOUND.getCode(), "PDF文件不存在或无法读取");
        }
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(properties.getBucket())
                .build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(properties.getBucket())
                    .build());
        }
    }

    private long parseContentLength(String value) {
        if (value == null || value.trim().isEmpty()) {
            return -1L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    @Getter
    @AllArgsConstructor
    public static class StoredObject {
        private final InputStream inputStream;
        private final long contentLength;
        private final String contentType;
    }
}
