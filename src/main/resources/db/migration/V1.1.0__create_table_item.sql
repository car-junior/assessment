CREATE TABLE IF NOT EXISTS dbo.item
(
    id         UUID           NOT NULL DEFAULT UUID_GENERATE_V4(),
    name       VARCHAR(255)   NOT NULL,
    type       VARCHAR(7)     NOT NULL,
    status     VARCHAR(8)     NOT NULL,
    price      DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP      NOT NULL,
    updated_at TIMESTAMP      NOT NULL,
    PRIMARY KEY (id)
);

-- Constraint Check min price
DO
$$
    BEGIN
        IF NOT EXISTS(
                SELECT 1
                FROM pg_constraint
                WHERE conname = 'item_check_min_price'
            ) THEN
            ALTER TABLE dbo.item
                ADD CONSTRAINT item_check_min_price CHECK (price > 0);
        END IF;
    END
$$;

-- Constraint unique name by type
DO
$$
    BEGIN
        IF NOT EXISTS(
                SELECT 1
                FROM pg_constraint
                WHERE conname = 'item_unique_name_by_type'
            ) THEN
            ALTER TABLE dbo.item
                ADD CONSTRAINT item_unique_name_by_type UNIQUE (name, type);
        END IF;
    END
$$;
