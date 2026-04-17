package com.khanh.handler;

import org.json.JSONObject;

import com.khanh.service.TransactionService;

public class TransactionHandler {
    public String handle(JSONObject req) {
        try {
            JSONObject body = req.optJSONObject("body");
            JSONObject query = req.optJSONObject("query");
            long senderId = body.getLong("senderId");
            long receiverId = body.getLong("receiverId");
            long amount = body.getLong("amount");
            long billId = body.optLong("billId", 0);

            boolean res = new TransactionService().transfer(senderId, receiverId, amount, billId);
            return new JSONObject().put("status", res ? "SUCCESS" : "FAIL").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject().put("status", "ERROR").put("message", "error in transaction handler").toString();
        }
    }
}
