package com.khanh.controller;

import org.json.JSONObject;

import com.khanh.service.AccountService;
import com.khanh.util.ResponseUtil;

public class AccountController {
    public String getBalance(JSONObject req) {
        try {
            JSONObject body = req.optJSONObject("body");
            JSONObject query = req.optJSONObject("query");
            long id = query.getLong("userId");

            long res = new AccountService().getBalance(id);

            return ResponseUtil.response(200, "ok", new JSONObject().put("balance", res));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.response(500, "error in get user balance", null);
        }
    }
}
