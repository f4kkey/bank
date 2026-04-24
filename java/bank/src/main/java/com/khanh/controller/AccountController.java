package com.khanh.controller;

import java.nio.channels.AcceptPendingException;

import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.khanh.exception.AccountNotFoundException;
import com.khanh.service.AccountService;
import com.khanh.util.ResponseUtil;

public class AccountController {
    public String getBalance(JSONObject req) {
        try {
            JSONObject body = req.optJSONObject("body");
            JSONObject query = req.optJSONObject("query");
            long id = query.getLong("userId");

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
