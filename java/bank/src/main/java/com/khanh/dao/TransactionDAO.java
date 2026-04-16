package com.khanh.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.khanh.model.Transaction;

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

    public List<Transaction> getTransactionsHistoryList() throws Exception {
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
}