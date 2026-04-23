// package com.khanh.util;

// import io.github.cdimascio.dotenv.Dotenv;
// import io.minio.MinioClient;
// import io.minio.PutObjectArgs;

// public class MinIOUtil {
// private static final Dotenv dotenv = Dotenv.load();

// private static final String ENDPOINT = dotenv.get("MINIO_URL");
// private static final String ACCESS_KEY = dotenv.get("MINIO_USER");
// private static final String SECRET_KEY = dotenv.get("MINIO_PASSWORD");;

// public static MinioClient minioClient;

// static {
// minioClient = MinioClient.builder()
// .endpoint(ENDPOINT)
// .credentials(ACCESS_KEY, SECRET_KEY)
// .build();
// }

// public static void upload(String bucket, String objectName,
// java.io.InputStream stream,
// String contentType) throws Exception {

// minioClient.putObject(
// PutObjectArgs.builder()
// .bucket(bucket)
// .object(objectName)
// .stream(stream, stream.available(), -1)
// .contentType(contentType)
// .build());
// }
// }