package com.khanh.controller;

import org.json.JSONObject;

import com.khanh.service.AccountService;

public class AccountController {
    public String getBalance(JSONObject req) {
        try {
            JSONObject body = req.optJSONObject("body");
            JSONObject query = req.optJSONObject("query");
            long id = query.getLong("userId");

            long res = new AccountService().getBalance(id);
            return new JSONObject().put("status", "SUCCESS").put("balance", res).toString();

        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject().put("status", "ERROR").put("message", "error in get user balance")
                    .toString();
        }
    }
}
