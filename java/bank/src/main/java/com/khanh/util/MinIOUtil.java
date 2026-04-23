package com.khanh.util;

import java.io.ByteArrayInputStream;

import io.github.cdimascio.dotenv.Dotenv;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

public class MinIOUtil {
    private static final Dotenv dotenv = Dotenv.load();

    private static final String ENDPOINT = dotenv.get("MINIO_URL");
    private static final String ACCESS_KEY = dotenv.get("MINIO_USER");
    private static final String SECRET_KEY = dotenv.get("MINIO_PASSWORD");;

    public static MinioClient minioClient;

    static {
        minioClient = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(ACCESS_KEY, SECRET_KEY)
                .build();
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