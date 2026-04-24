package com.khanh.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ResponseUtil {

    public static String response(int code, String message, Object data) {
        JsonObject obj = new JsonObject();

        obj.addProperty("code", code);
        obj.addProperty("message", message);

        if (data != null) {
            obj.add("data", new Gson().toJsonTree(data));
        }

        return obj.toString();
    }
}
