package com.khanh.util;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

/**
 * Lightweight HashiCorp Vault client that reads secrets from Vault's KV v2
 * engine using the HTTP API. Authentication is done via a root/dev token
 * supplied through the VAULT_TOKEN environment variable.
 *
 * Retries with exponential backoff so the app can tolerate Vault starting
 * slightly later than the Java process.
 */
public class VaultClient {

    private static final String VAULT_ADDR =
            System.getenv("VAULT_ADDR") != null ? System.getenv("VAULT_ADDR") : "http://vault:8200";

    private static final String VAULT_TOKEN =
            System.getenv("VAULT_TOKEN") != null ? System.getenv("VAULT_TOKEN") : "root";

    private static final int MAX_RETRIES = 10;
    private static final long RETRY_DELAY_MS = 3000;

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

    /**
     * Fetches all key-value pairs stored at the given KV v2 secret path.
     *
     * @param secretPath Vault KV v2 path, e.g. {@code "secret/data/bank"}
     * @return map of secret key → value
     * @throws RuntimeException if Vault is unreachable after all retries
     */
    public static Map<String, String> getSecrets(String secretPath) {
        String url = VAULT_ADDR + "/v1/" + secretPath;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("X-Vault-Token", VAULT_TOKEN)
                        .get()
                        .build();

                try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        System.err.printf("[VaultClient] Attempt %d/%d – HTTP %d from %s%n",
                                attempt, MAX_RETRIES, response.code(), url);
                    } else {
                        String body = response.body().string();
                        return parseKvV2(body);
                    }
                }
            } catch (Exception e) {
                System.err.printf("[VaultClient] Attempt %d/%d – %s%n",
                        attempt, MAX_RETRIES, e.getMessage());
            }

            if (attempt < MAX_RETRIES) {
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        throw new RuntimeException(
                "[VaultClient] Failed to fetch secrets from Vault at " + url
                + " after " + MAX_RETRIES + " attempts.");
    }

    /**
     * Parses a KV v2 response body and returns the {@code data.data} map.
     *
     * KV v2 response shape:
     * <pre>
     * {
     *   "data": {
     *     "data": { "key1": "val1", "key2": "val2" },
     *     "metadata": { ... }
     *   }
     * }
     * </pre>
     */
    private static Map<String, String> parseKvV2(String json) {
        JSONObject root = new JSONObject(json);
        JSONObject data = root.getJSONObject("data").getJSONObject("data");

        Map<String, String> secrets = new HashMap<>();
        for (String key : data.keySet()) {
            secrets.put(key, data.getString(key));
        }
        return secrets;
    }
}
