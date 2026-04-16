package com.khanh.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.khanh.util.DBconnnection;

import com.khanh.model.Account;

public class AccountDAO {
    private Connection conn;

    public AccountDAO(Connection conn) {
        this.conn = conn;
    }

    public Account getById(long id) throws Exception {
        String sql = "select * from accounts where id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, id);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Account(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getLong("balance"));
        }
        return null;
    }

    public void updateBalance(long id, long newBalance) throws Exception {
        String sql = "update accounts set balance = ? where id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, newBalance);
        ps.setLong(2, id);
        ps.executeUpdate();
    }

    public static void main(String[] args) {
        try {
            Connection conn = DBconnnection.getConnection();
            AccountDAO accountDAO = new AccountDAO(conn);
            Account account = accountDAO.getById(4);
            System.out.println(account.getName() + " has balance: " + account.getBalance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
