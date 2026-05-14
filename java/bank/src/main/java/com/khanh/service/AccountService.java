package com.khanh.service;

import java.sql.Connection;

import com.khanh.dao.AccountDAO;
import com.khanh.exception.AccountNotFoundException;
import com.khanh.model.Account;
import com.khanh.util.DBconnnection;

public class AccountService {
    public void createAccount(long id, String name, String role) {
        try {
            Connection conn = DBconnnection.getConnection();
            AccountDAO accountDAO = new AccountDAO(conn);
            accountDAO.createAccount(id, name, role);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating account");
        }
    }

    public long getBalance(long id) {
        try {
            Connection conn = DBconnnection.getConnection();
            AccountDAO accountDAO = new AccountDAO(conn);
            Account account = accountDAO.getById(id);
            if (account == null) {
                throw new AccountNotFoundException("Account not found");
            }
            return account.getBalance();
        } catch (AccountNotFoundException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching account balance");
        }
    }
}
