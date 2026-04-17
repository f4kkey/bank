package com.khanh;

import org.json.JSONObject;

public class Router {
    public String route(String request) {
        try {
            JSONObject req = new JSONObject(request);

            String method = req.getString("method");
            String path = req.getString("path");

            JSONObject body = req.optJSONObject("body");
            JSONObject query = req.optJSONObject("query");

            System.out.println(request);

            if (method.equals("GET") && path.equals("/test"))
                return new JSONObject().put("status", "SUCCESS").put("message", "API call").toString();

            return new JSONObject().put("status", "ERROR").put("message", "Unknown API").toString();
        } catch (

        Exception e) {
            e.printStackTrace();
            return new JSONObject().put("status", "ERROR").put("message", "Invalid request").toString();
        }
    }
}
