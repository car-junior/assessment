CREATE TABLE IF NOT EXISTS dbo.order_items
(
    id         UUID           NOT NULL DEFAULT UUID_GENERATE_V4(),
    order_id   UUID           NOT NULL,
    item_id    UUID           NOT NULL,
    item_price DECIMAL(10, 2) NOT NULL,
    amount     INT4           NOT NULL,
    created_at TIMESTAMP      NOT NULL,
    updated_at TIMESTAMP      NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (order_id) REFERENCES dbo.order,
    FOREIGN KEY (item_id) REFERENCES dbo.item
);

ALTER TABLE dbo.order_items
    ADD CONSTRAINT order_items_check_min_amount CHECK (amount >= 1);