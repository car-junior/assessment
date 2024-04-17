CREATE TABLE IF NOT EXISTS dbo.item
(
    id         UUID           NOT NULL DEFAULT UUID_GENERATE_V4(),
    name       VARCHAR(255)   NOT NULL,
    type       VARCHAR(7)     NOT NULL,
    status     VARCHAR(10)    NOT NULL,
    price      DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP      NOT NULL,
    updated_at TIMESTAMP      NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE dbo.item
    ADD CONSTRAINT item_check_min_price CHECK (price > 0);