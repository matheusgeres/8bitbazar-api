package com.eightbitbazar.config.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

@Component
public class MinioHealthIndicator implements HealthIndicator {

    private final S3Client s3Client;
    private final String bucketName;

    public MinioHealthIndicator(
            S3Client s3Client,
            @Value("${minio.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public Health health() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build());

            return Health.up()
                    .withDetail("bucket", bucketName)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("bucket", bucketName)
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
