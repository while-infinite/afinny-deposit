CREATE SEQUENCE hibernate_sequence START 1;

CREATE TABLE IF NOT EXISTS account
(
    id              UUID           PRIMARY KEY,
    account_number  VARCHAR(30)    NOT NULL,
    client_id       UUID           NOT NULL,
    currency_code   CHAR(3)        NOT NULL,
    current_balance NUMERIC(19, 4) NOT NULL,
    open_date       DATE           NOT NULL,
    close_date      DATE           NOT NULL,
    is_active       BOOLEAN        NOT NULL DEFAULT TRUE,
    salary_project  VARCHAR(30),
    blocked_sum     NUMERIC(19, 4) NOT NULL,
    CONSTRAINT close_date_check    CHECK ( close_date > open_date ),
    CONSTRAINT blocked_sum_check   CHECK ( current_balance >= blocked_sum )
);

CREATE TABLE IF NOT EXISTS product
(
    id                  INTEGER          PRIMARY KEY,
    name                VARCHAR(30)      NOT NULL,
    schema_name         VARCHAR(30)      NOT NULL,
    interest_rate_early NUMERIC(6, 4),
    is_capitalization   BOOLEAN          NOT NULL DEFAULT FALSE,
    amount_min          NUMERIC(19, 4),
    amount_max          NUMERIC(19, 4),
    currency_code       CHAR(3)          NOT NULL,
    is_active           BOOLEAN          NOT NULL DEFAULT TRUE,
    is_revocable        BOOLEAN          NOT NULL DEFAULT FALSE,
    min_interest_rate   NUMERIC(6, 4)    NOT NULL,
    max_interest_rate   NUMERIC(6, 4)    NOT NULL,
    min_duration_months INTEGER          NOT NULL,
    max_duration_months INTEGER          NOT NULL,
    active_since        DATE,
    active_until        DATE,
    CONSTRAINT amount_max_check          CHECK ( amount_max >= amount_min ),
    CONSTRAINT max_interest_rate_check   CHECK ( max_interest_rate >= min_interest_rate ),
    CONSTRAINT max_duration_months_check CHECK ( max_duration_months >= min_duration_months )
);

CREATE TABLE IF NOT EXISTS agreement
(
    id               UUID                     PRIMARY KEY,
    agreement_number VARCHAR(20)              NOT NULL,
    account_id       UUID                     NOT NULL REFERENCES account (id),
    product_id       INTEGER                  NOT NULL REFERENCES product (id),
    interest_rate    NUMERIC(6, 4)            NOT NULL,
    start_date       TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date         TIMESTAMP WITH TIME ZONE NOT NULL,
    initial_amount   NUMERIC(19, 4)           NOT NULL,
    current_balance  NUMERIC(19, 4)           NOT NULL,
    is_active        BOOLEAN                  NOT NULL DEFAULT TRUE,
    auto_renewal     BOOLEAN                  NOT NULL DEFAULT FALSE,
    CONSTRAINT end_date_check                 CHECK ( end_date > start_date )
);

CREATE TABLE IF NOT EXISTS operation_type
(
    id       SERIAL       PRIMARY KEY,
    type     VARCHAR(30),
    is_debit BOOLEAN      NOT NULL
);

INSERT INTO operation_type (type, is_debit)
VALUES ('REPLENISHMENT', true);
INSERT INTO operation_type (type, is_debit)
VALUES ('PAYMENT', false);
INSERT INTO operation_type (type, is_debit)
VALUES ('TRANSFER', false);
INSERT INTO operation_type (type, is_debit)
VALUES ('OTHER_EXPENDITURE', false);

CREATE TABLE IF NOT EXISTS operation
(
    id                UUID                        PRIMARY KEY,
    account_id        UUID                        NOT NULL REFERENCES account (id),
    completed_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    sum               NUMERIC(19, 4)              NOT NULL,
    details           TEXT,
    currency_code     CHAR(3)                     NOT NULL,
    operation_type_id INTEGER                     REFERENCES operation_type (id)
);

CREATE TABLE IF NOT EXISTS card_product
(
    id             SERIAL         PRIMARY KEY ,
    card_name      VARCHAR(30) UNIQUE NOT NULL,
    payment_system VARCHAR(30)    NOT NULL,
    cashback       NUMERIC(6, 4),
    co_brand       VARCHAR(30),
    is_virtual     BOOLEAN,
    premium_status VARCHAR(30)    NOT NULL,
    service_price  numeric(19, 4) NOT NULL,
    product_price  numeric(19, 4) NOT NULL,
    currency_code  CHAR(3)        NOT NULL,
    is_active      BOOLEAN        NOT NULL DEFAULT TRUE,
    card_duration  INTEGER
);

CREATE TABLE IF NOT EXISTS card
(
    id                UUID             PRIMARY KEY,
    card_number       CHAR(16)         NOT NULL,
    account_id        UUID             NOT NULL REFERENCES account (id),
    transaction_limit NUMERIC(19, 4),
    status            VARCHAR(30)      NOT NULL,
    expiration_date   DATE             NOT NULL,
    holder_name       VARCHAR(50)      NOT NULL,
    digital_wallet    VARCHAR(30),
    is_default        BOOLEAN          NOT NULL DEFAULT TRUE,
    card_product_id   INTEGER          REFERENCES card_product (id),
    balance           DECIMAL(19,4)    DEFAULT 0,
    CONSTRAINT transaction_limit_check CHECK ( transaction_limit >= 0 ),
    CONSTRAINT expiration_date_check   CHECK ( now() < expiration_date )
);
