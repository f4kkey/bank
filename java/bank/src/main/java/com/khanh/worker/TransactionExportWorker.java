package com.khanh.worker;

import java.sql.Connection;
import java.util.List;

import com.google.gson.Gson;
import com.khanh.dao.SystemStateDAO;
import com.khanh.dao.TransactionDAO;
import com.khanh.model.Transaction;
import com.khanh.util.DBconnnection;
import com.khanh.util.MinIOUtil;

public class TransactionExportWorker implements Runnable {
    private static final int INTERVAL_SECONDS = 30;

    @Override
    public void run() {
        System.out.println("[TransactionExportWorker] Started - update every " + INTERVAL_SECONDS + "s");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                processExporting();
            } catch (Exception e) {
                System.err.println("[TransactionExportWorker] Error during update: " + e.getMessage());
            }

            try {
                Thread.sleep(INTERVAL_SECONDS * 1000L);
            } catch (InterruptedException e) {
                System.out.println("[TransactionExportWorker] Interrupted - shutting down.");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processExporting() {
        try (Connection conn = DBconnnection.getConnection()) {
            SystemStateDAO systemStateDAO = new SystemStateDAO(conn);
            if (!systemStateDAO.isTransactionNeededUpdated())
                return;

            TransactionDAO transactionDAO = new TransactionDAO(conn);
            List<Transaction> list = transactionDAO.getTransactionsList();
            Gson gson = new Gson();
            String json = gson.toJson(list);

            MinIOUtil.upload("transactions", "transactions.json", json, "application/json");

            System.out.println("[TransactionExportWorker] Uploaded to MinIO!");

            systemStateDAO.removeTransactionUpdated();

        } catch (Exception e) {
            System.err.println("[TransactionExportWorker] Cannot fetch update from DB: " + e.getMessage());
            return;
        }

    }
}
