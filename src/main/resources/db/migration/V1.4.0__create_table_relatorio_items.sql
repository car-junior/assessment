CREATE TABLE IF NOT EXISTS dbo.relatorio_items
(
    id         UUID           NOT NULL DEFAULT UUID_GENERATE_V4(),
    item_id    UUID           NOT NULL,
    created_at TIMESTAMP      NOT NULL,
    updated_at TIMESTAMP      NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (item_id) REFERENCES dbo.item
);