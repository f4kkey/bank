-- Active: 1776306199883@@127.0.0.1@3306@bank
CREATE DATABASE IF NOT EXISTS bank;

use bank;

DROP TABLE IF EXISTS accounts;

DROP TABLE IF EXISTS transactions;

create table accounts (
    id BIGINT AUTO_INCREMENT primary key,
    name varchar(100),
    balance BIGINT DEFAULT 991000000 NOT NULL,
    role VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

create table transactions (
    id BIGINT auto_increment primary key,
    billId BIGINT DEFAULT -1,
    senderId BIGINT NOT NULL,
    receiverId BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    callback_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    callback_attempts INT NOT NULL DEFAULT 0,
    save BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_billId (billId),
    INDEX idx_senderId (senderId),
    INDEX idx_receiverId (receiverId)
);