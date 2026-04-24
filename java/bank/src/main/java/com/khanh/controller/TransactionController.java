package com.khanh.controller;

import java.util.List;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.khanh.exception.AccountNotFoundException;
import com.khanh.exception.ConnectErrorException;
import com.khanh.exception.DuplicateBillException;
import com.khanh.exception.InsufficientBalanceException;
import com.khanh.exception.InvalidRequestException;
import com.khanh.model.Transaction;
import com.khanh.service.TransactionService;
import com.khanh.util.ResponseUtil;

public class TransactionController {
    public String transfer(JSONObject req) {
        try {
            JSONObject body = req.optJSONObject("body");
            JSONObject query = req.optJSONObject("query");
            long senderId = body.getLong("senderId");
            long receiverId = body.getLong("receiverId");
            long amount = body.getLong("amount");
            long billId = body.optLong("billId", -1);

            new TransactionService().transfer(senderId, receiverId, amount, billId);
            return ResponseUtil.response(200, "ok", null);
        } catch (DuplicateBillException e) {
            return ResponseUtil.response(409, "DUPLICATE_BILL_ID", null);
        } catch (InsufficientBalanceException e) {
            return ResponseUtil.response(422, "INSUFFICIENT_BALANCE", null);
        } catch (AccountNotFoundException e) {
            return ResponseUtil.response(404, "ACCOUNT_NOT_FOUND", null);
        } catch (InvalidRequestException e) {
            return ResponseUtil.response(422, "INVALID_REQUEST", null);
        } catch (Exception e) {
            return ResponseUtil.response(500, "SYSTEM_ERROR", null);
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
            JsonObject data = new JsonObject();
            data.add("transactions", new Gson().toJsonTree(res));

            return ResponseUtil.response(200, "ok", data);
        } catch (AccountNotFoundException e) {
            return ResponseUtil.response(404, "ACCOUNT_NOT_FOUND", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.response(500, "SYSTEM_ERROR", null);
        }
    }

    public String getTransactionDetail(JSONObject req) {
        try {
            JSONObject body = req.optJSONObject("body");
            JSONObject query = req.optJSONObject("query");
            long billId = query.optLong("billId", -1);

            if (billId == -1) {
                return new JSONObject().put("status", "ERROR").put("message", "bill id must exists")
                        .toString();
            }

            String res = new TransactionService().getTransactionDetail(billId);
            JsonObject data = new JsonObject();
            data.addProperty("detail", res);
            return ResponseUtil.response(200, "ok", data);
        } catch (ConnectErrorException e) {
            return ResponseUtil.response(503, "SHOP_SERVER_UNAVAILABLE", null);
        } catch (Exception e) {
            return ResponseUtil.response(500, "SYSTEM_ERROR", null);
        }
    }
}
