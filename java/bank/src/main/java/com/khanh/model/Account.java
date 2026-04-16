package com.khanh.model;

public class Account {
    private long id;
    private String name;
    private long balance;
    private String role;

    public Account(long id, String name, long balance, String role) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
