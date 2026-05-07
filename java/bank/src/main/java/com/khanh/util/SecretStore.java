package com.khanh.util;

import java.util.Collections;
import java.util.Map;

public class SecretStore {

    private static final String SECRET_PATH = "secret/data/bank";

    private static final Map<String, String> SECRETS;

    static {
        System.out.println("[SecretStore] Loading secrets from Vault path: " + SECRET_PATH);
        Map<String, String> loaded = VaultClient.getSecrets(SECRET_PATH);
        SECRETS = Collections.unmodifiableMap(loaded);
        System.out.println("[SecretStore] Loaded " + SECRETS.size() + " secrets successfully.");
    }

    public static String get(String key) {
        return SECRETS.get(key);
    }

    public static String getOrDefault(String key, String defaultValue) {
        String value = SECRETS.get(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }
}
