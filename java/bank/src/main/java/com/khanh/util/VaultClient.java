package com.khanh.util;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

public class VaultClient {

    private static final String VAULT_ADDR = System.getenv("VAULT_ADDR") != null ? System.getenv("VAULT_ADDR")
            : "http://vault:8200";

    private static final String VAULT_TOKEN = System.getenv("VAULT_TOKEN") != null ? System.getenv("VAULT_TOKEN")
            : "root";

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 3000;

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

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
                        System.err.printf("[VaultClient] Attempt %d/%d - HTTP %d from %s%n",
                                attempt, MAX_RETRIES, response.code(), url);
                    } else {
                        String body = response.body().string();
                        return parseKvV2(body);
                    }
                }
            } catch (Exception e) {
                System.err.printf("[VaultClient] Attempt %d/%d - %s%n",
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
