package com.khanh.service;

import java.sql.Connection;

import com.khanh.dao.AccountDAO;
import com.khanh.model.Account;
import com.khanh.util.DBconnnection;

public class AccountService {
    public long getBalance(long id) {
        try {
            Connection conn = DBconnnection.getConnection();
            AccountDAO accountDAO = new AccountDAO(conn);
            Account account = accountDAO.getById(id);
            return account.getBalance();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
