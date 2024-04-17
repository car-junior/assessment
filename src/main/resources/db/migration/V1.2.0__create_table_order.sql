CREATE TABLE IF NOT EXISTS dbo.order
(
    id         UUID       NOT NULL DEFAULT UUID_GENERATE_V4(),
    status     VARCHAR(7) NOT NULL,
    discount   NUMERIC(3, 2),
    created_at TIMESTAMP  NOT NULL,
    updated_at TIMESTAMP  NOT NULL,
    PRIMARY KEY (id)
);

ALTER TABLE dbo.order
    ADD CONSTRAINT order_check_discount_range CHECK (discount IS NULL OR (discount > 0 AND discount <= 1));