package com.khanh.service;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.units.qual.A;

import com.khanh.dao.AccountDAO;
import com.khanh.dao.TransactionDAO;
import com.khanh.util.DBconnnection;
import com.khanh.util.Redis;
import com.khanh.model.*;

public class TransactionService {
    Connection conn = null;

    public boolean transfer(long senderId, long receiverId, long amount, long billId) {
        String redisKey = "bill:" + billId;
        if (billId != -1) {
            boolean locked = Redis.lock(redisKey, 300);
            if (!locked) {
                System.out.println("Duplicate billId detected");
                return true;
            }
        }

        try {
            conn = DBconnnection.getConnection();
            conn.setAutoCommit(false); // rollback

            AccountDAO accountDAO = new AccountDAO(conn);
            TransactionDAO transactionDAO = new TransactionDAO(conn);

            if (billId != -1) {
                boolean exists = transactionDAO.findTransactionByBillId(billId);
                if (exists) {
                    System.out.println("Duplicate billId detected");
                    conn.close();
                    return true;
                }
            }

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

            transactionDAO.addTransaction(billId, senderId, receiverId, amount);

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (billId != -1) {
                Redis.delete("bill:" + billId);
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean transfer(long senderId, long receiverId, long amount) {
        return transfer(senderId, receiverId, amount, -1);
    }

    public List<Transaction> getTransactionsList() {
        try {
            Connection conn = DBconnnection.getConnection();
            TransactionDAO transactionDAO = new TransactionDAO(conn);
            return transactionDAO.getTransactionsList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Transaction> getPersonalTransactionsList(long userId, long transactionId, long billId) {
        try {
            Connection conn = DBconnnection.getConnection();
            TransactionDAO transactionDAO = new TransactionDAO(conn);
            return transactionDAO.getPersonalTransactionsList(userId, transactionId, billId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        TransactionService service = new TransactionService();
        boolean success = service.transfer(2, 3, 1500000);
        if (success) {
            System.out.println("Transfer successful");
            try {
                AccountDAO accountDAO = new AccountDAO(DBconnnection.getConnection());
                Account sender = accountDAO.getById(4);
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
        System.out.println(service.getTransactionsList());
    }
}
