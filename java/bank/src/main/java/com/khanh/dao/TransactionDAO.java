package com.khanh.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

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
}