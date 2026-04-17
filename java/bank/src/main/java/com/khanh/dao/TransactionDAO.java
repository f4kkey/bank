package com.khanh.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.khanh.model.Transaction;
import com.khanh.util.DBconnnection;

public class TransactionDAO {
    private Connection conn;

    public TransactionDAO(Connection conn) {
        this.conn = conn;
    }

    public void addTransaction(long senderId, long receiverId, long amount) throws Exception {
        String sql = "insert into transactions (senderId, receiverId, amount) values (?,?,?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, senderId);
        ps.setLong(2, receiverId);
        ps.setLong(3, amount);
        ps.executeUpdate();
    }

    public List<Transaction> getTransactionsList() throws Exception {
        List<Transaction> res = new ArrayList<>();

        String sql = "select * from transactions order by created_at desc";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            res.add(
                    new Transaction(rs.getLong("id"), rs.getLong("senderId"), rs.getLong("receiverId"),
                            rs.getLong("amount"), rs.getTimestamp("created_at")));
        }
        return res;
    }

    public List<Transaction> getPersonalTransactionsList(long id) throws Exception {
        List<Transaction> res = new ArrayList<>();

        String sql = "select * from transactions where senderId = ? OR receiverId = ? ORDER BY created_at desc";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, id);
        ps.setLong(2, id);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            res.add(
                    new Transaction(rs.getLong("id"), rs.getLong("senderId"), rs.getLong("receiverId"),
                            rs.getLong("amount"), rs.getTimestamp("created_at")));
        }
        return res;
    }

    public static void main(String[] arg) throws Exception {
        Connection conn = DBconnnection.getConnection();
        TransactionDAO transactionDAO = new TransactionDAO(conn);
        System.out.println(transactionDAO.getPersonalTransactionsList(2));

    }
}