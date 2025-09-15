CREATE TYPE user_role AS ENUM ('CUSTOMER', 'ADMIN', 'STAFF', 'MODERATOR');

CREATE TYPE order_status AS ENUM ('PENDING', 'COMPLETED', 'CANCELLED', 'SHIPPED', 'DELIVERED', 'RETURNED');

CREATE TYPE payment_status AS ENUM ('PENDING', 'SUCCESS', 'FAILED');

-- =========================
-- Users & Addresses
-- =========================
CREATE TABLE users
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username      VARCHAR(100) UNIQUE NOT NULL,
    email         VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    phone_number  VARCHAR(20),
    role          user_role        DEFAULT 'CUSTOMER',
    is_active     BOOLEAN          DEFAULT TRUE,
    updated_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE addresses
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL,
    full_name    VARCHAR(150),
    phone_number VARCHAR(20),
    city         VARCHAR(100) NOT NULL,
    ward         VARCHAR(100) NOT NULL,
    street       TEXT,
    created_at   TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_addresses_users FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

-- =========================
-- Categories & Products & Variants
-- =========================
CREATE TABLE categories
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    brand       VARCHAR(100),
    image_url   TEXT,
    is_active   BOOLEAN          DEFAULT TRUE,
    updated_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id)
        REFERENCES categories (id) ON DELETE SET NULL
);
CREATE TABLE product_variants
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID           NOT NULL,
    color      VARCHAR(50),
    size       VARCHAR(50),
    price      DECIMAL(12, 2) NOT NULL,
    stock      INT              DEFAULT 0,
    created_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_variant_product FOREIGN KEY (product_id)
        REFERENCES products (id) ON DELETE CASCADE
);

-- =========================
-- Orders & Items
-- =========================
CREATE TABLE orders
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID                NOT NULL,
    address_id   UUID,
    total_price  DECIMAL(12, 2)      NOT NULL,
    status       order_status     DEFAULT 'PENDING',
    order_number VARCHAR(100) UNIQUE NOT NULL,
    created_at   TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    note         TEXT,
    CONSTRAINT fk_orders_users FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_orders_addresses FOREIGN KEY (address_id)
        REFERENCES addresses (id) ON DELETE SET NULL
);

CREATE TABLE order_items
(
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id           UUID           NOT NULL,
    product_variant_id UUID           NOT NULL,
    quantity           INT            NOT NULL,
    unit_price         DECIMAL(12, 2) NOT NULL,
    subtotal_price     DECIMAL(12, 2) NOT NULL,
    created_at         TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_items_orders FOREIGN KEY (order_id)
        REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_variants FOREIGN KEY (product_variant_id)
        REFERENCES product_variants (id) ON DELETE CASCADE
);

-- =========================
-- Payments
-- =========================
CREATE TABLE payments
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id       UUID           NOT NULL,
    amount         DECIMAL(12, 2) NOT NULL,
    payment_method VARCHAR(50)    NOT NULL,            -- VNPay, MoMo, COD, ...
    payment_status payment_status   DEFAULT 'PENDING', -- SUCCESS, FAILED, PENDING
    transaction_id VARCHAR(100),
    created_at     TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_orders FOREIGN KEY (order_id)
        REFERENCES orders (id) ON DELETE CASCADE
);

-- =========================
-- AI Try-on Sessions
-- =========================
CREATE TABLE tryon_sessions
(
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID,
    selected_product_id UUID,
    image_input_url     TEXT,
    output_result_url   TEXT,
    status              VARCHAR(50)      DEFAULT 'PROCESSING',
    device_info         TEXT,
    created_at          TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tryon_user FOREIGN KEY (user_id)
        REFERENCES users (id),
    CONSTRAINT fk_tryon_product FOREIGN KEY (selected_product_id)
        REFERENCES products (id)
);

-- =========================
-- API Clients & Logs
-- =========================
CREATE TABLE api_clients
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_name     VARCHAR(150) NOT NULL,
    email           VARCHAR(150),
    api_key         VARCHAR(255) NOT NULL UNIQUE,
    status          VARCHAR(50)      DEFAULT 'ACTIVE',
    request_limit   INT              DEFAULT 1000,
    usage_count     INT              DEFAULT 0,
    last_request_at TIMESTAMP,
    ip_address      VARCHAR(100),
    organization    VARCHAR(150),
    latency_ms      INT,
    created_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE api_usage_logs
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id       UUID NOT NULL,
    endpoint        VARCHAR(200),
    request_time    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    response_status VARCHAR(50),
    CONSTRAINT fk_apilogs_client FOREIGN KEY (client_id)
        REFERENCES api_clients (id) ON DELETE CASCADE
);



