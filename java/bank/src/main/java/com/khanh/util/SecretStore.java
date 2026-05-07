package com.khanh.util;

import java.util.Collections;
import java.util.Map;

/**
 * Application-wide secret store backed by HashiCorp Vault.
 *
 * Secrets are fetched once at class-load time via {@link VaultClient} and
 * cached for the lifetime of the JVM process. All other utilities
 * (DBconnnection, MinIOUtil, RedisUtil) read from this store instead of
 * querying environment variables directly.
 */
public class SecretStore {

    /** Vault KV v2 path where all bank secrets are stored. */
    private static final String SECRET_PATH = "secret/data/bank";

    private static final Map<String, String> SECRETS;

    static {
        System.out.println("[SecretStore] Loading secrets from Vault path: " + SECRET_PATH);
        Map<String, String> loaded = VaultClient.getSecrets(SECRET_PATH);
        SECRETS = Collections.unmodifiableMap(loaded);
        System.out.println("[SecretStore] Loaded " + SECRETS.size() + " secrets successfully.");
    }

    /**
     * Returns the value for the given secret key, or {@code null} if absent.
     *
     * @param key secret key name as stored in Vault (e.g. {@code "mariadb_url"})
     * @return secret value, or {@code null}
     */
    public static String get(String key) {
        return SECRETS.get(key);
    }

    /**
     * Returns the value for the given secret key, falling back to
     * {@code defaultValue} when the key is absent or blank.
     *
     * @param key          secret key name
     * @param defaultValue fallback value
     * @return secret value or default
     */
    public static String getOrDefault(String key, String defaultValue) {
        String value = SECRETS.get(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }
}
