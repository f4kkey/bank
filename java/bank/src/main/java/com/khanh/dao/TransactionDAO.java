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

    public void addTransaction(long billId, long senderId, long receiverId, long amount) throws Exception {
        String sql = "insert into transactions (billId, senderId, receiverId, amount) values (?,?,?,?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, billId);
        ps.setLong(2, senderId);
        ps.setLong(3, receiverId);
        ps.setLong(4, amount);
        ps.executeUpdate();
    }

    public List<Transaction> getTransactionsList() throws Exception {
        List<Transaction> res = new ArrayList<>();

        String sql = "select * from transactions order by created_at desc";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            res.add(
                    new Transaction(rs.getLong("id"), rs.getLong("billId"), rs.getLong("senderId"),
                            rs.getLong("receiverId"),
                            rs.getLong("amount"), rs.getTimestamp("created_at")));
        }
        return res;
    }

    public List<Transaction> getPersonalTransactionsList(long userId, long transactionId, long billId)
            throws Exception {
        List<Transaction> res = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT * FROM transactions WHERE (senderId = ? OR receiverId = ?)");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        params.add(userId);
        if (transactionId != -1) {
            sql.append(" AND id = ?");
            params.add(transactionId);
        }
        if (billId != -1) {
            sql.append(" AND billId = ?");
            params.add(billId);
        }
        sql.append(" ORDER BY created_at DESC");

        PreparedStatement ps = conn.prepareStatement(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            res.add(
                    new Transaction(rs.getLong("id"), rs.getLong("billId"), rs.getLong("senderId"),
                            rs.getLong("receiverId"),
                            rs.getLong("amount"), rs.getTimestamp("created_at")));
        }
        return res;
    }

    public static void main(String[] arg) throws Exception {
        Connection conn = DBconnnection.getConnection();
        TransactionDAO transactionDAO = new TransactionDAO(conn);
        System.out.println(transactionDAO.getPersonalTransactionsList(2, -1, -1));

    }
}