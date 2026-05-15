package com.khanh.controller;

import java.nio.channels.AcceptPendingException;

import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.khanh.exception.AccountNotFoundException;
import com.khanh.service.AccountService;
import com.khanh.util.ResponseUtil;

public class AccountController {
    public String createAccount(JSONObject req) {
        try {
            JSONObject body = req.optJSONObject("body");
            JSONObject query = req.optJSONObject("query");
            long id = body.getLong("id");
            String name = body.getString("name");
            String role = body.getString("role");
            System.out.println("Creating account: " + id + ", " + name + ", " + role);
            new AccountService().createAccount(id, name, role);
            return ResponseUtil.response(200, "OK", null);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.response(500, "SYSTEM_ERROR", null);
        }
    }

    public String getBalance(JSONObject req) {
        try {
            JSONObject body = req.optJSONObject("body");
            JSONObject query = req.optJSONObject("query");
            long id = query.getLong("userId");
            if (req.has("userId") && req.getLong("userId") != id && !"ADMIN".equals(req.optString("role", ""))) {
                return ResponseUtil.response(403, "Forbidden: Cannot access other user's account", null);
            }

            long res = new AccountService().getBalance(id);
            JsonObject data = new JsonObject();
            data.addProperty("balance", res);
            return ResponseUtil.response(200, "OK", data);

        } catch (AccountNotFoundException e) {
            e.printStackTrace();
            return ResponseUtil.response(404, "ACCOUNT_NOT_FOUND", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.response(500, "SYSTEM_ERROR", null);
        }
    }
}
