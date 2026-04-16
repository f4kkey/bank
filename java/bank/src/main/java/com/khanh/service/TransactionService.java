package com.khanh.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.units.qual.A;

import com.khanh.dao.AccountDAO;
import com.khanh.dao.TransactionDAO;
import com.khanh.util.DBconnnection;
import com.khanh.model.*;

public class TransactionService {
    public boolean transfer(long senderId, long receiverId, long amount) {
        try {
            Connection conn = DBconnnection.getConnection();
            conn.setAutoCommit(false); // rollback

            AccountDAO accountDAO = new AccountDAO(conn);
            TransactionDAO transactionDAO = new TransactionDAO(conn);

            long first = Math.min(senderId, receiverId);
            long second = Math.max(senderId, receiverId);

            accountDAO.lockAccounts(first, second); // deadlock

            Account sender = accountDAO.getById(senderId);
            Account receiver = accountDAO.getById(receiverId);

            if (amount <= 0 || sender == null || receiver == null || senderId == receiverId
                    || (sender.getBalance() < amount && !sender.getRole().equals("admin"))) {
                conn.rollback();
                return false;
            }

            if (!sender.getRole().equals("admin")) {
                sender.setBalance(sender.getBalance() - amount);
                accountDAO.updateBalance(senderId, sender.getBalance());
            }

            if (!receiver.getRole().equals("admin")) {
                receiver.setBalance(receiver.getBalance() + amount);
                accountDAO.updateBalance(receiverId, receiver.getBalance());
            }

            transactionDAO.addTransaction(senderId, receiverId, amount);

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaction> getTransactionsHistoryList() {
        try {
            Connection conn = DBconnnection.getConnection();
            TransactionDAO transactionDAO = new TransactionDAO(conn);
            return transactionDAO.getTransactionsHistoryList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        TransactionService service = new TransactionService();
        boolean success = service.transfer(1, 2, 1000000);
        if (success) {
            System.out.println("Transfer successful");
            try {
                AccountDAO accountDAO = new AccountDAO(DBconnnection.getConnection());
                Account sender = accountDAO.getById(1);
                Account receiver = accountDAO.getById(2);
                System.out.println(sender.getName() + " has balance: " +
                        sender.getBalance());
                System.out.println(receiver.getName() + " has balance: " +
                        receiver.getBalance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Transfer failed");
        }
        System.out.println(service.getTransactionsHistoryList());
    }
}
