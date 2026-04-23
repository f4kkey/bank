package com.khanh.worker;

import java.sql.Connection;
import java.util.List;

import com.khanh.dao.TransactionDAO;
import com.khanh.model.Transaction;
import com.khanh.service.TransactionService;
import com.khanh.util.DBconnnection;

public class CallbackWorker implements Runnable {
    private static final int INTERVAL_SECONDS = 30;
    private static final int MAX_ATTEMPTS = 10;

    @Override
    public void run() {
        System.out.println("[CallbackWorker] Started - ping every "
                + INTERVAL_SECONDS + "s, max attempts=" + MAX_ATTEMPTS);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                processPendingCallbacks();
            } catch (Exception e) {
                System.err.println("[CallbackWorker] Error during polling round: " + e.getMessage());
            }

            try {
                Thread.sleep(INTERVAL_SECONDS * 1000L);
            } catch (InterruptedException e) {
                System.out.println("[CallbackWorker] Interrupted - shutting down.");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processPendingCallbacks() {
        List<Transaction> pending;

        try (Connection conn = DBconnnection.getConnection()) {
            pending = new TransactionDAO(conn).getPendingCallbacks(MAX_ATTEMPTS);
        } catch (Exception e) {
            System.err.println("[CallbackWorker] Cannot fetch pending callbacks from DB: " + e.getMessage());
            return;
        }

        if (pending.isEmpty()) {
            return;
        }

        System.out.println("[CallbackWorker] Found " + pending.size() + " pending callback to retry.");
        TransactionService service = new TransactionService();

        for (Transaction tx : pending) {
            System.out.println("[CallbackWorker] Retrying callback for billId=" + tx.getBillId());
            service.notifyShop(tx.getBillId(), true);
        }
    }
}
