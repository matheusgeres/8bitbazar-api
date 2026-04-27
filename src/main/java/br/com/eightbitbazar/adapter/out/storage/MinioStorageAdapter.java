package br.com.eightbitbazar.adapter.out.storage;

import br.com.eightbitbazar.application.port.out.ImageStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
public class MinioStorageAdapter implements ImageStorage {

    private final S3Client s3Client;
    private final String endpoint;
    private final String bucket;

    public MinioStorageAdapter(
            S3Client s3Client,
            @Value("${minio.endpoint}") String endpoint,
            @Value("${minio.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.endpoint = endpoint;
        this.bucket = bucket;
    }

    @Override
    public String upload(String filename, InputStream inputStream, String contentType, long size) {
        log.info("storage.upload.started",
            kv("filename", filename),
            kv("contentType", contentType),
            kv("sizeBytes", size));
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(filename)
                .contentType(contentType)
                .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, size));

            String url = getUrl(filename);
            log.info("storage.upload.completed",
                kv("filename", filename),
                kv("sizeBytes", size));
            return url;
        } catch (Exception e) {
            log.error("storage.upload.failed",
                kv("filename", filename),
                kv("error", e.getMessage() != null ? e.getMessage() : e.toString()));
            throw e;
        }
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
