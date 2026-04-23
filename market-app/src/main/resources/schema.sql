CREATE TABLE IF NOT EXISTS images (
    id BIGSERIAL PRIMARY KEY,
    content_type VARCHAR(100) NOT NULL,
    bytes BYTEA NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS items (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    price BIGINT NOT NULL CHECK (price >= 0),
    image_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_items_image
        FOREIGN KEY (image_id)
        REFERENCES images (id)
        ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_items_title ON items (title);

CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_sum BIGINT NOT NULL CHECK (total_sum >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders (user_id);

CREATE TABLE IF NOT EXISTS cart_item_counts (
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    count INTEGER NOT NULL CHECK (count > 0),
    PRIMARY KEY (user_id, item_id),
    CONSTRAINT fk_cart_item_counts_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_cart_item_counts_item
        FOREIGN KEY (item_id)
        REFERENCES items (id)
        ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_cart_item_counts_user_id ON cart_item_counts (user_id);

CREATE TABLE IF NOT EXISTS order_item_counts (
    order_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    count INTEGER NOT NULL CHECK (count > 0),
    PRIMARY KEY (order_id, item_id),
    CONSTRAINT fk_order_item_counts_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_order_item_counts_item
        FOREIGN KEY (item_id)
        REFERENCES items (id)
        ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_order_item_counts_order_id ON order_item_counts (order_id);
