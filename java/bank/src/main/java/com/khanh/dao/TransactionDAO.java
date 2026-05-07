package com.khanh.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.khanh.model.Transaction;
import com.khanh.util.DBconnnection;

public class TransactionDAO {
    private Connection conn;

    public TransactionDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean findTransactionByBillId(long billId) throws Exception {
        String sql = "select * from transactions where billId  = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, billId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return true;
        }
        return false;
    };

    public Long addTransaction(long billId, long senderId, long receiverId, long amount) throws Exception {
        String sql = "insert into transactions (billId, senderId, receiverId, amount) values (?,?,?,?)";
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setLong(1, billId);
        ps.setLong(2, senderId);
        ps.setLong(3, receiverId);
        ps.setLong(4, amount);
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            return rs.getLong(1);
        } else {
            throw new Exception("Cannot get generated transaction id");
        }
    }

    public void updateCallbackStatus(long billId, String status) throws Exception {
        String sql = "UPDATE transactions " +
                "SET callback_status = ?, " +
                "    callback_attempts = callback_attempts + 1 " +
                "WHERE billId = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, status);
        ps.setLong(2, billId);
        ps.executeUpdate();
    }

    public List<Transaction> getPendingCallbacks(int maxAttempts) throws Exception {
        List<Transaction> res = new ArrayList<>();
        String sql = "SELECT * FROM transactions " +
                "WHERE callback_status != 'SENT' " +
                "  AND billId != -1 " +
                "  AND callback_attempts < ? " +
                "ORDER BY created_at ASC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, maxAttempts);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            res.add(new Transaction(
                    rs.getLong("id"),
                    rs.getLong("billId"),
                    rs.getLong("senderId"),
                    rs.getLong("receiverId"),
                    rs.getLong("amount"),
                    rs.getTimestamp("created_at")));
        }
        return res;
    }

    public List<Transaction> getTransactionsList() throws Exception {
        List<Transaction> res = new ArrayList<>();

        String sql = "select * from transactions order by created_at desc";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            res.add(
                    new Transaction(rs.getLong("id"),
                            rs.getLong("billId"),
                            rs.getLong("senderId"),
                            rs.getLong("receiverId"),
                            rs.getLong("amount"),
                            rs.getTimestamp("created_at")));
        }
        return res;
    }

    public List<Transaction> getPersonalTransactionsList(long userId, long transactionId, long billId, String startDate,
            String endDate) throws Exception {
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
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND created_at >= ?");
            params.add(Timestamp.valueOf(startDate + " 00:00:00"));
        }

        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND created_at <= ?");
            params.add(Timestamp.valueOf(endDate + " 23:59:59"));
        }
        sql.append(" ORDER BY created_at DESC");

        PreparedStatement ps = conn.prepareStatement(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            res.add(
                    new Transaction(
                            rs.getLong("id"),
                            rs.getLong("billId"),
                            rs.getLong("senderId"),
                            rs.getLong("receiverId"),
                            rs.getLong("amount"),
                            rs.getTimestamp("created_at")));
        }
        return res;
    }

    public List<Transaction> getUnsaveTransactions() throws Exception {
        List<Transaction> res = new ArrayList<>();
        String sql = "select id from transaction where save = false ORDER by created_at DESC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            res.add(
                    new Transaction(
                            rs.getLong("id"),
                            rs.getLong("billId"),
                            rs.getLong("senderId"),
                            rs.getLong("receiverId"),
                            rs.getLong("amount"),
                            rs.getTimestamp("created_at")));
        }
        return res;
    }

    public void markTransactionAsSaved(long id) throws Exception {
        String sql = "update transaction set save = true where id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, id);
        ps.executeUpdate();
    }

    public static void main(String[] arg) throws Exception {
        Connection conn = DBconnnection.getConnection();
        TransactionDAO transactionDAO = new TransactionDAO(conn);
        System.out.println(transactionDAO.getPersonalTransactionsList(2, -1, -1, null, null));

    }
}