package com.khanh.dao;

import java.sql.*;

public class SystemStateDAO {

    private Connection conn;

    public SystemStateDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean isTransactionNeededUpdated() throws SQLException {
        String sql = "SELECT state_value FROM system_state WHERE state_name='transaction'";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("state_value") == 1;
        }
        return false;
    }

    public void addTransactionUpdated() throws SQLException {
        String sql = "UPDATE system_state SET state_value=1 WHERE state_name='transaction'";
        conn.prepareStatement(sql).executeUpdate();
    }

    public void removeTransactionUpdated() throws SQLException {
        String sql = "UPDATE system_state SET state_value=0 WHERE state_name='transaction'";
        conn.prepareStatement(sql).executeUpdate();
    }
}
