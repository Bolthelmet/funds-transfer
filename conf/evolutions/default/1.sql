
# --- !Ups

CREATE TABLE user_account (
    id                      VARCHAR(30)          PRIMARY KEY,
    funds_euro              NUMERIC(15,2)    NOT NULL
);

CREATE TABLE fund_transaction (
    sender            VARCHAR(30),
    reciever          VARCHAR(30),
    funds_euro        NUMERIC(15,2),

    CONSTRAINT user_account_sender_fk FOREIGN KEY (sender) REFERENCES user_account(id),
    CONSTRAINT user_account_reciever_fk FOREIGN KEY (reciever) REFERENCES user_account(id)
);

# --- !Downs

DROP TABLE IF EXISTS user_account;
DROP TABLE IF EXISTS fund_transaction;
