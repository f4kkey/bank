package com.khanh.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

public class RedisUtil {
    private static final String HOST = "localhost";
    private static final int PORT = 6379;

    public static boolean lock(String key, long timeout) {
        try (Jedis jedis = new Jedis(HOST, PORT)) {
            SetParams setParams = new SetParams();
            setParams.nx();
            setParams.ex(timeout);

            String res = jedis.set(key, "locked", setParams);
            return "OK".equals(res);
        }
    }

    public static void delete(String key) {
        try (Jedis jedis = new Jedis(HOST, PORT)) {
            jedis.del(key);
        }
    }
}
