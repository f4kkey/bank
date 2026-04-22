package com.khanh.controller;

import java.security.Timestamp;
import java.util.List;

import org.json.JSONObject;

import com.khanh.model.Transaction;
import com.khanh.service.TransactionService;

public class TransactionController {
    public String transfer(JSONObject req) {
        try {
            JSONObject body = req.optJSONObject("body");
            JSONObject query = req.optJSONObject("query");
            long senderId = body.getLong("senderId");
            long receiverId = body.getLong("receiverId");
            long amount = body.getLong("amount");
            long billId = body.optLong("billId", -1);

            boolean res = new TransactionService().transfer(senderId, receiverId, amount, billId);
            return new JSONObject().put("status", res ? "SUCCESS" : "FAIL").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject().put("status", "ERROR").put("message", "error in transaction transfer")
                    .toString();
        }
    }

    public String getPersonalTransactionsList(JSONObject req) {
        try {
            JSONObject body = req.optJSONObject("body");
            JSONObject query = req.optJSONObject("query");

            long userId = query.getLong("userId");
            long transactionId = query.optLong("transactionId", -1);
            long billId = query.optLong("billId", -1);
            // Timestamp startDate = query.has("userId") ? query.get("userId") : null;

            List<Transaction> res = new TransactionService().getPersonalTransactionsList(userId, transactionId, billId);

            return new JSONObject().put("status", "SUCCESS").put("transactions", res).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject().put("status", "ERROR").put("message", "error in transaction list")
                    .toString();
        }
    }
}
