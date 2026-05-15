package com.khanh;

import java.util.Arrays;

import org.json.JSONObject;

import com.khanh.controller.AccountController;
import com.khanh.controller.TransactionController;
import com.khanh.util.ResponseUtil;

public class Router {

    @FunctionalInterface
    private interface RouteHandler {
        String handle(JSONObject req) throws Exception;
    }

    // Middleware: Enforce JWT authentication
    private String authentication(JSONObject req, RouteHandler next) throws Exception {
        if (!req.has("userId")) {
            return ResponseUtil.response(401, "Unauthorized: Missing User Context", null);
        }
        return next.handle(req);
    }

    // Middleware: Enforce Internal HMAC authentication
    private String hmac(JSONObject req, RouteHandler next) throws Exception {
        JSONObject headers = req.optJSONObject("headers");
        String authSource = headers != null ? headers.optString("X-Internal-Source", "") : "";
        if (!"HMAC".equals(authSource)) {
            return ResponseUtil.response(403, "Forbidden: internal API only", null);
        }
        return next.handle(req);
    }

    // Middleware: Enforce Role Authorization (automatically implies Authentication)
    private String authorization(JSONObject req, String[] requiredRole, RouteHandler next) throws Exception {
        if (!req.has("userId")) {
            return ResponseUtil.response(401, "Unauthorized: Missing User Context", null);
        }
        if (!req.has("role") || !Arrays.asList(requiredRole).contains(req.optString("role", ""))) {
            return ResponseUtil.response(403, "Forbidden: Insufficient Permissions", null);
        }
        return next.handle(req);
    }

    public String route(String request) {
        try {
            JSONObject req = new JSONObject(request);

            String method = req.getString("method");
            String path = req.getString("path");

            JSONObject headers = req.optJSONObject("headers");
            String userIdStr = headers != null ? headers.optString("X-User-Id", "") : "";
            String userRole = headers != null ? headers.optString("X-User-Role", "") : "";

            if (!userIdStr.isEmpty()) {
                req.put("userId", Long.parseLong(userIdStr));
                req.put("role", userRole);
            }

            System.out.println(request);
            System.out
                    .println("Parsed Request: method=" + method + ", path=" + path + ", userId=" + userIdStr + ", role="
                            + userRole);

            // Public routes
            if (method.equals("GET") && path.equals("/test"))
                return ResponseUtil.response(200, "OK", null);

            // Internal routes (HMAC protected)
            if (method.equals("POST") && path.equals("/user/create"))
                return hmac(req, r -> new AccountController().createAccount(r));

            // Protected routes (JWT authenticated)
            if (method.equals("POST") && path.equals("/transfer"))
                return authentication(req, r -> new TransactionController().transfer(r));

            if (method.equals("GET") && path.equals("/user/balance"))
                return authentication(req, r -> new AccountController().getBalance(r));

            if (method.equals("GET") && path.equals("/transactions"))
                return authentication(req, r -> new TransactionController().getPersonalTransactionsList(r));

            if (method.equals("GET") && path.equals("/transactions/detail"))
                return authentication(req, r -> new TransactionController().getTransactionDetail(r));

            return new JSONObject().put("status", "ERROR").put("message", "Unknown API").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject().put("status", "ERROR").put("message", "Invalid request").toString();
        }
    }
}
