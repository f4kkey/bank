package com.khanh.service;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.time.Duration;
import java.util.List;

import com.google.gson.JsonObject;
import com.khanh.dao.AccountDAO;
import com.khanh.dao.TransactionDAO;
import com.khanh.exception.AccountNotFoundException;
import com.khanh.exception.ConnectErrorException;
import com.khanh.exception.DuplicateBillException;
import com.khanh.exception.InsufficientBalanceException;
import com.khanh.exception.InternalServerErrorException;
import com.khanh.exception.InvalidRequestException;
import com.khanh.util.DBconnnection;
import com.khanh.util.HmacUtil;
import com.khanh.util.RedisUtil;
import com.khanh.model.*;
import com.khanh.util.SecretStore;

public class TransactionService {
    public long transfer(long senderId, long receiverId, long amount, long billId, long currentUserId,
            String currentUserRole) {
        if (billId != -1) {
            String redisKey = "bill:" + billId;
            boolean locked = RedisUtil.lock(redisKey, 300);
            if (!locked) {
                System.out.println("Duplicate billId detected");
                throw new DuplicateBillException("Duplicate billId detected");
            }
        }

        try (Connection conn = DBconnnection.getConnection();) {
            conn.setAutoCommit(false);

            AccountDAO accountDAO = new AccountDAO(conn);
            TransactionDAO transactionDAO = new TransactionDAO(conn);

            if (billId != -1) {
                boolean exists = transactionDAO.findTransactionByBillId(billId);
                if (exists) {
                    System.out.println("Duplicate billId detected");
                    throw new DuplicateBillException("Duplicate billId detected");
                }
            }

            long first = Math.min(senderId, receiverId);
            long second = Math.max(senderId, receiverId);

            accountDAO.lockAccounts(first, second); // deadlock

            Account sender = accountDAO.getById(senderId);
            Account receiver = accountDAO.getById(receiverId);

            if (sender == null || receiver == null) {
                conn.rollback();
                throw new AccountNotFoundException("Sender or receiver account not found");
            }
            if (amount <= 0 || senderId == receiverId) {
                conn.rollback();
                throw new InvalidRequestException("Invalid data");
            }

            if ((sender.getBalance() < amount && !sender.getRole().equals("ADMIN"))) {
                conn.rollback();
                throw new InsufficientBalanceException("Insufficient balance");
            }

            if (!currentUserRole.equals("ADMIN") && currentUserId != senderId) {
                conn.rollback();
                throw new InvalidRequestException("Cannot transfer from other user's account");
            }

            if (!sender.getRole().equals("ADMIN")) {
                sender.setBalance(sender.getBalance() - amount);
                accountDAO.updateBalance(senderId, sender.getBalance());
            }

            if (!receiver.getRole().equals("ADMIN")) {
                receiver.setBalance(receiver.getBalance() + amount);
                accountDAO.updateBalance(receiverId, receiver.getBalance());
            }

            long res = transactionDAO.addTransaction(billId, senderId, receiverId, amount);
            conn.commit();

            if (billId != -1) {
                final long fBillId = billId;
                final boolean fSuccess = true;
                Thread notifyThread = new Thread(() -> notifyShop(fBillId, fSuccess));
                notifyThread.setDaemon(true);
                notifyThread.start();
            }
            return res;
        } catch (DuplicateBillException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            if (billId != -1) {
                RedisUtil.delete("bill:" + billId);
            }
            if (e instanceof AccountNotFoundException || e instanceof InvalidRequestException
                    || e instanceof InsufficientBalanceException) {
                throw (RuntimeException) e;
            }
            throw new InternalServerErrorException("Internal server error");
        }
    }

    public void notifyShop(long billId, boolean success) {
        String shopUrl = SecretStore.get("server_shop_url");
        String url = shopUrl + "/bill/" + billId + "/payment-result";

        String jsonBody = "{\"billId\":" + billId + ",\"success\":" + success + "}";

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            // Sign the request with HMAC for inter-service authentication
            String timestamp = String.valueOf(System.currentTimeMillis());
            String signature = HmacUtil.sign(timestamp, jsonBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("X-Internal-Signature", signature)
                    .header("X-Internal-Timestamp", timestamp)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String newStatus;
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                newStatus = "SENT";
                System.out.println("[Callback] Shop notified for billId=" + billId);
            } else {
                newStatus = "PENDING";
                System.err.println("[Callback] Shop returned HTTP " + response.statusCode()
                        + " for billId=" + billId + " left PENDING");
            }

            try (Connection statusConn = DBconnnection.getConnection()) {
                new TransactionDAO(statusConn).updateCallbackStatus(billId, newStatus);
            }

        } catch (ConnectException | java.net.http.HttpConnectTimeoutException e) {
            System.err.println("[Callback] Shop unreachable for billId=" + billId
                    + " left PENDING for retry. (" + e.getMessage() + ")");
        } catch (Exception e) {
            System.err.println("[Callback] Unexpected error for billId=" + billId + ": " + e.getMessage());
        }
    }

    public List<Transaction> getTransactionsList() {
        try (Connection conn = DBconnnection.getConnection();) {
            TransactionDAO transactionDAO = new TransactionDAO(conn);
            return transactionDAO.getTransactionsList();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching transactions list");
        }
    }

    public List<Transaction> getPersonalTransactionsList(long userId, long transactionId, long billId, String startDate,
            String endDate, long currentUserId, String currentUserRole) {
        if (userId == -1) {
            if (currentUserRole.equals("ADMIN")) {
                return getTransactionsList();
            } else {
                throw new InvalidRequestException("userId is required");
            }
        }
        try (Connection conn = DBconnnection.getConnection();) {
            if (!currentUserRole.equals("ADMIN") && currentUserId != userId) {
                throw new InvalidRequestException("Cannot view other user's transactions");
            }
            Account account = new AccountDAO(conn).getById(userId);
            if (account == null) {
                throw new AccountNotFoundException("User account not found");
            }
            TransactionDAO transactionDAO = new TransactionDAO(conn);
            return transactionDAO.getPersonalTransactionsList(userId, transactionId, billId, startDate, endDate);
        } catch (InvalidRequestException e) {
            throw e;
        } catch (AccountNotFoundException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching personal transactions list");
        }
    }

    public String getTransactionDetail(long billId, long currentUserId, String currentUserRole) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = SecretStore.get("server_shop_url") + "/orders/" + billId;
            // Sign the request with HMAC for inter-service authentication
            // JsonObject body = new JsonObject();
            String timestamp = String.valueOf(System.currentTimeMillis());
            String signature = HmacUtil.sign(timestamp, "");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("Accept", "application/json")
                    .header("X-Internal-Signature", signature)
                    .header("X-Internal-Timestamp", timestamp)
                    .header("X-User-Id", String.valueOf(currentUserId))
                    .header("X-User-Role", currentUserRole)
                    .build();
            System.out.println("Fetching transaction detail for billId=" + billId + " with userId=" + currentUserId
                    + " and role=" + currentUserRole);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body(); // JSON string
            }
            throw new RuntimeException("Failed with status: " + response.statusCode());
        } catch (ConnectException e) {
            throw new ConnectErrorException("Cannot connect to shop server");
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerErrorException(null);
        }
    }

    public static void main(String[] args) {
        TransactionService service = new TransactionService();
        System.out.println("Transfer successful");
        try {
            AccountDAO accountDAO = new AccountDAO(DBconnnection.getConnection());
            Account sender = accountDAO.getById(4);
            Account receiver = accountDAO.getById(2);
            System.out.println(sender.getName() + " has balance: " +
                    sender.getBalance());
            System.out.println(receiver.getName() + " has balance: " +
                    receiver.getBalance());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error fetching account details");
        }
        System.out.println(service.getTransactionsList());
    }

}
