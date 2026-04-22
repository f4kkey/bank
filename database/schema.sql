use bank;

drop table if exists accounts;

drop table if exists transactions;

create table accounts (
    id BIGINT auto_increment primary key,
    name varchar(100),
    balance BIGINT DEFAULT 1000000 NOT NULL,
    role VARCHAR(10) NOT NULL
);

create table transactions (
    id BIGINT auto_increment primary key,
    billId BIGINT,
    senderId BIGINT NOT NULL,
    receiverId BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_billId ON transactions (billId);

CREATE INDEX idx_senderId ON transactions (senderId);

CREATE INDEX idx_receiverId ON transactions (receiverId);

insert into
    accounts (name, role)
values ('user1', 'user'),
    ('user2', 'user'),
    ('user3', 'user'),
    ('admin', 'admin');