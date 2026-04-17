package com.khanh;

import org.json.JSONObject;

import com.khanh.controller.AccountController;
import com.khanh.controller.TransactionController;

public class Router {
    public String route(String request) {
        try {
            JSONObject req = new JSONObject(request);

            String method = req.getString("method");
            String path = req.getString("path");

            System.out.println(request);

            if (method.equals("GET") && path.equals("/test"))
                return new JSONObject().put("status", "SUCCESS").put("message", "API call").toString();
            if (method.equals("POST") && path.equals("/transfer"))
                return new TransactionController().transfer(req);
            if (method.equals("GET") && path.equals("/user/balance"))
                return new AccountController().getBalance(req);
            if (method.equals("GET") && path.equals("/user/transactions"))
                return new TransactionController().getPersonalTransactionsList(req);

            return new JSONObject().put("status", "ERROR").put("message", "Unknown API").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject().put("status", "ERROR").put("message", "Invalid request").toString();
        }
    }
}
