use bank;

drop table if exists accounts;
drop table if exists transactions;

create table accounts (
    id BIGINT auto_increment primary key,
    name varchar(100),
    balance BIGINT
    role VARCHAR(10)
);

create table transactions (
    id BIGINT auto_increment primary key,
    senderId BIGINT,
    receiverId BIGINT,
    amount BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

insert into accounts (name, balance) values
    ('user1', 10000),
    ('user2', 20000),
    ('user3', 40000),
    ('admin', 0);