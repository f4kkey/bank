package com.khanh.util;

import java.io.ByteArrayInputStream;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

public class MinIOUtil {

        public static MinioClient minioClient;

        static {
                try {
                        String endpoint = SecretStore.get("minio_url");
                        String accessKey = SecretStore.get("minio_user");
                        String secretKey = SecretStore.get("minio_password");

                        minioClient = MinioClient.builder()
                                        .endpoint(endpoint)
                                        .credentials(accessKey, secretKey)
                                        .build();

                        String bucket = "transactions";

                        boolean found = minioClient.bucketExists(
                                        BucketExistsArgs.builder()
                                                        .bucket(bucket)
                                                        .build());

                        if (!found) {
                                minioClient.makeBucket(
                                                MakeBucketArgs.builder()
                                                                .bucket(bucket)
                                                                .build());
                        }

                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
        }

        public static void upload(String bucket, String objectName,
                        String json,
                        String contentType) throws Exception {

                minioClient.putObject(
                                PutObjectArgs.builder()
                                                .bucket(bucket)
                                                .object(objectName)
                                                .stream(new ByteArrayInputStream(json.getBytes()),
                                                                json.length(),
                                                                -1)
                                                .contentType(contentType)
                                                .build());
        }
}