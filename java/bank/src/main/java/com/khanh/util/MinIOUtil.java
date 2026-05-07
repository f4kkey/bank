package com.khanh.util;

import java.io.ByteArrayInputStream;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

public class MinIOUtil {

        private static final String ENDPOINT = System.getenv("MINIO_URL");
        private static final String ACCESS_KEY = System.getenv("MINIO_USER");
        private static final String SECRET_KEY = System.getenv("MINIO_PASSWORD");

        public static MinioClient minioClient;

        static {
                try {
                        minioClient = MinioClient.builder()
                                        .endpoint(ENDPOINT)
                                        .credentials(ACCESS_KEY, SECRET_KEY)
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