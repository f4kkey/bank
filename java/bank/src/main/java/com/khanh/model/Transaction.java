package com.khanh.model;

import java.sql.Timestamp;

public class Transaction {
    private long id;
    private long senderId;
    private long receiverId;
    private long amount;
    private Timestamp createdAt;

    public Transaction(long id, long senderId, long receiverId, long amount, Timestamp createdAt) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(long receiverId) {
        this.receiverId = receiverId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Transaction [id=" + id + ", senderId=" + senderId + ", receiverId=" + receiverId + ", amount=" + amount
                + ", createdAt=" + createdAt + "]";
    }
}
