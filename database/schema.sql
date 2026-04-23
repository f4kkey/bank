use bank;

drop table if exists accounts;

drop table if exists transactions;

drop table if exists system_state;

create table accounts (
    id BIGINT auto_increment primary key,
    name varchar(100),
    balance BIGINT DEFAULT 1000000 NOT NULL,
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_billId (billId),
    INDEX idx_senderId (senderId),
    INDEX idx_receiverId (receiverId)
);

CREATE Table system_state (
    state_name VARCHAR(50) PRIMARY KEY,
    state_value INT DEFAULT 0
);

INSERT INTO
    system_state (state_name, state_value)
VALUES ('transaction', 0);

insert into
    accounts (name, role)
values ('user1', 'user'),
    ('user2', 'user'),
    ('user3', 'user'),
    ('admin', 'admin');