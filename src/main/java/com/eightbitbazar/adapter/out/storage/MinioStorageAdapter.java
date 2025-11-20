package com.eightbitbazar.adapter.out.storage;

import com.eightbitbazar.application.port.out.ImageStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URI;

@Component
public class MinioStorageAdapter implements ImageStorage {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucket;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        this.s3Client = S3Client.builder()
            .endpointOverride(URI.create(endpoint))
            .region(Region.US_EAST_1)
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            ))
            .forcePathStyle(true)
            .build();
    }

    @Override
    public String upload(String filename, InputStream inputStream, String contentType, long size) {
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(filename)
            .contentType(contentType)
            .build();

        s3Client.putObject(request, RequestBody.fromInputStream(inputStream, size));

        return getUrl(filename);
    }

    @Override
    public void delete(String filename) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(filename)
            .build();

        s3Client.deleteObject(request);
    }

    @Override
    public String getUrl(String filename) {
        return endpoint + "/" + bucket + "/" + filename;
    }
}
