package com.khanh.service;

import java.sql.Connection;

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

            Account sender = accountDAO.getById(senderId);
            Account receiver = accountDAO.getById(receiverId);

            if (amount <= 0 || sender == null || receiver == null || sender.getBalance() < amount) {
                return false;
            }

            sender.setBalance(sender.getBalance() - amount);
            receiver.setBalance(receiver.getBalance() + amount);

            accountDAO.updateBalance(senderId, sender.getBalance());
            accountDAO.updateBalance(receiverId, receiver.getBalance());

            transactionDAO.addTransaction(senderId, receiverId, amount);

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
                System.out.println(sender.getName() + " has balance: " + sender.getBalance());
                System.out.println(receiver.getName() + " has balance: " + receiver.getBalance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Transfer failed");
        }
    }
}
