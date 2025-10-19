-- V1__init_schema.sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE user_role AS ENUM ('CUSTOMER', 'ADMIN', 'STAFF', 'MODERATOR');
CREATE TYPE order_status AS ENUM ('PENDING', 'COMPLETED', 'CANCELLED', 'SHIPPED', 'DELIVERED', 'RETURNED');
CREATE TYPE payment_status AS ENUM ('PENDING', 'SUCCESS', 'FAILED');

-- Users & Addresses
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       username VARCHAR(100) UNIQUE NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       phone_number VARCHAR(20),
                       role user_role DEFAULT 'CUSTOMER',
                       is_active BOOLEAN DEFAULT TRUE,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE addresses (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           user_id UUID NOT NULL,
                           full_name VARCHAR(150),
                           phone_number VARCHAR(20),
                           city VARCHAR(100) NOT NULL,
                           ward VARCHAR(100) NOT NULL,
                           street TEXT,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_addresses_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Categories & Products & Variants
CREATE TABLE categories (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            name VARCHAR(100) UNIQUE NOT NULL,
                            description TEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          category_id UUID,
                          name VARCHAR(255) NOT NULL,
                          description TEXT,
                          brand VARCHAR(100),
                          image_url TEXT,
                          is_active BOOLEAN DEFAULT TRUE,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL
);

CREATE TABLE product_variants (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  product_id UUID NOT NULL,
                                  color VARCHAR(50),
                                  size VARCHAR(50),
                                  price DECIMAL(12, 2) NOT NULL,
                                  stock INT DEFAULT 0,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  CONSTRAINT fk_variant_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);

-- Orders & Items
CREATE TABLE orders (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        user_id UUID NOT NULL,
                        address_id UUID,
                        total_price DECIMAL(12, 2) NOT NULL,
                        status order_status DEFAULT 'PENDING',
                        order_number VARCHAR(100) UNIQUE NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        completed_at TIMESTAMP,
                        note TEXT,
                        CONSTRAINT fk_orders_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                        CONSTRAINT fk_orders_addresses FOREIGN KEY (address_id) REFERENCES addresses (id) ON DELETE SET NULL
);

CREATE TABLE order_items (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             order_id UUID NOT NULL,
                             product_variant_id UUID NOT NULL,
                             quantity INT NOT NULL,
                             unit_price DECIMAL(12, 2) NOT NULL,
                             subtotal_price DECIMAL(12, 2) NOT NULL,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT fk_order_items_orders FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
                             CONSTRAINT fk_order_items_variants FOREIGN KEY (product_variant_id) REFERENCES product_variants (id) ON DELETE CASCADE
);

-- Payments
CREATE TABLE payments (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          order_id UUID NOT NULL,
                          amount DECIMAL(12, 2) NOT NULL,
                          payment_method VARCHAR(50) NOT NULL,
                          payment_status payment_status DEFAULT 'PENDING',
                          transaction_id VARCHAR(100),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_payments_orders FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

-- AI Try-on Sessions
CREATE TABLE tryon_sessions (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id UUID,
                                selected_product_id UUID,
                                image_input_url TEXT,
                                output_result_url TEXT,
                                status VARCHAR(50) DEFAULT 'PROCESSING',
                                device_info TEXT,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT fk_tryon_user FOREIGN KEY (user_id) REFERENCES users (id),
                                CONSTRAINT fk_tryon_product FOREIGN KEY (selected_product_id) REFERENCES products (id)
);

-- API Clients & Logs
CREATE TABLE api_clients (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             client_name VARCHAR(150) NOT NULL,
                             email VARCHAR(150),
                             api_key VARCHAR(255) NOT NULL UNIQUE,
                             status VARCHAR(50) DEFAULT 'ACTIVE',
                             request_limit INT DEFAULT 1000,
                             usage_count INT DEFAULT 0,
                             last_request_at TIMESTAMP,
                             ip_address VARCHAR(100),
                             organization VARCHAR(150),
                             latency_ms INT,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE api_usage_logs (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                client_id UUID NOT NULL,
                                endpoint VARCHAR(200),
                                request_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                response_status VARCHAR(50),
                                CONSTRAINT fk_apilogs_client FOREIGN KEY (client_id) REFERENCES api_clients (id) ON DELETE CASCADE
);

-- Wishlist & Wishlist Items
CREATE TABLE wishlists (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           user_id UUID NOT NULL,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_wishlists_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE wishlist_items (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                wishlist_id UUID NOT NULL,
                                product_variant_id UUID NOT NULL,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT fk_wishlist_items_wishlist FOREIGN KEY (wishlist_id) REFERENCES wishlists (id) ON DELETE CASCADE,
                                CONSTRAINT fk_wishlist_items_variant FOREIGN KEY (product_variant_id) REFERENCES product_variants (id) ON DELETE CASCADE
);

-- Cart & Cart Items
CREATE TABLE carts (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       user_id UUID NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT fk_carts_users FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE cart_items (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            cart_id UUID NOT NULL,
                            product_variant_id UUID NOT NULL,
                            quantity INT NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE,
                            CONSTRAINT fk_cart_items_variant FOREIGN KEY (product_variant_id) REFERENCES product_variants (id) ON DELETE CASCADE
);


-- Coupons
CREATE TABLE coupons (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         code VARCHAR(50) UNIQUE NOT NULL,
                         discount_type VARCHAR(50) NOT NULL,
                         discount_value DECIMAL(12, 2) NOT NULL,
                         max_uses INT DEFAULT 1,
                         current_uses INT DEFAULT 0,
                         expires_at TIMESTAMP,
                         is_active BOOLEAN DEFAULT TRUE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- V2__fix_user_role.sql content
ALTER TABLE users ALTER COLUMN role TYPE user_role USING role::user_role;

-- V20251010152116__add_bank_code_to_payments.sql content
ALTER TABLE payments ADD COLUMN bank_code VARCHAR(50);
CREATE INDEX idx_payment_transaction_id ON payments(transaction_id);
CREATE INDEX idx_payment_status ON payments(payment_status);

-- V20251007150717__add_column_unit_price_for_cart_items.sql content
ALTER TABLE cart_items ADD COLUMN unit_price DECIMAL(12,2) NOT NULL DEFAULT 0;
