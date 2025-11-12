-- =====================================================
-- Flyway Migration V1: Initial Schema
-- Archivo: src/main/resources/db/migration/V1__initial_schema.sql
-- =====================================================

-- =====================================================
-- TABLA: customers
-- =====================================================
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(2) NOT NULL DEFAULT 'MX',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_phone_format CHECK (phone ~ '^\+?[0-9]{10,15}$')
);

CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_phone ON customers(phone);
CREATE INDEX idx_customers_uuid ON customers(uuid);

-- =====================================================
-- TABLA: tokenized_cards
-- =====================================================
CREATE TABLE tokenized_cards (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    customer_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    last_four_digits CHAR(4) NOT NULL,
    card_brand VARCHAR(20) NOT NULL,
    cardholder_name VARCHAR(255) NOT NULL,
    expiration_month SMALLINT NOT NULL,
    expiration_year SMALLINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_default BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,

    CONSTRAINT fk_tokenized_cards_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT chk_expiration_month CHECK (expiration_month BETWEEN 1 AND 12),
    CONSTRAINT chk_expiration_year CHECK (expiration_year >= EXTRACT(YEAR FROM CURRENT_DATE)),
    CONSTRAINT chk_card_brand CHECK (card_brand IN ('VISA', 'MASTERCARD', 'AMEX', 'DISCOVER'))
);

CREATE INDEX idx_tokenized_cards_customer ON tokenized_cards(customer_id);
CREATE INDEX idx_tokenized_cards_token ON tokenized_cards(token);
CREATE UNIQUE INDEX idx_one_default_card_per_customer ON tokenized_cards(customer_id)
    WHERE is_default = true AND is_active = true;

-- =====================================================
-- TABLA: products
-- =====================================================
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    sku VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(12, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    category VARCHAR(100),
    brand VARCHAR(100),
    image_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_price_positive CHECK (price >= 0),
    CONSTRAINT chk_stock_non_negative CHECK (stock >= 0)
);

CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_brand ON products(brand);
CREATE INDEX idx_products_is_active ON products(is_active) WHERE is_active = true;
CREATE INDEX idx_products_stock ON products(stock) WHERE is_active = true;
CREATE INDEX idx_products_name_trgm ON products USING gin(name gin_trgm_ops);

-- =====================================================
-- TABLA: product_searches
-- =====================================================
CREATE TABLE product_searches (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    customer_id BIGINT,
    search_query VARCHAR(500) NOT NULL,
    results_count INT NOT NULL DEFAULT 0,
    ip_address INET,
    user_agent TEXT,
    searched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_product_searches_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE SET NULL
);

CREATE INDEX idx_product_searches_customer ON product_searches(customer_id);
CREATE INDEX idx_product_searches_date ON product_searches(searched_at DESC);
CREATE INDEX idx_product_searches_query ON product_searches USING gin(search_query gin_trgm_ops);

-- =====================================================
-- TABLA: shopping_carts
-- =====================================================
CREATE TABLE shopping_carts (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    customer_id BIGINT,
    session_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    total_amount NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    total_items INT NOT NULL DEFAULT 0,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL '72 hours'),

    CONSTRAINT fk_shopping_carts_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT chk_cart_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'CONVERTED')),
    CONSTRAINT chk_customer_or_session CHECK (
        (customer_id IS NOT NULL) OR (session_id IS NOT NULL)
    )
);

CREATE INDEX idx_shopping_carts_customer ON shopping_carts(customer_id) WHERE customer_id IS NOT NULL;
CREATE INDEX idx_shopping_carts_session ON shopping_carts(session_id) WHERE session_id IS NOT NULL;
CREATE INDEX idx_shopping_carts_status ON shopping_carts(status);
CREATE INDEX idx_shopping_carts_expires_at ON shopping_carts(expires_at) WHERE status = 'ACTIVE';
CREATE UNIQUE INDEX idx_active_cart_per_customer ON shopping_carts(customer_id)
    WHERE status = 'ACTIVE' AND customer_id IS NOT NULL;
CREATE UNIQUE INDEX idx_active_cart_per_session ON shopping_carts(session_id)
    WHERE status = 'ACTIVE' AND session_id IS NOT NULL;

-- =====================================================
-- TABLA: cart_items
-- =====================================================
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price NUMERIC(12, 2) NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL GENERATED ALWAYS AS (quantity * unit_price) STORED,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id)
        REFERENCES shopping_carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE RESTRICT,
    CONSTRAINT chk_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_unit_price_positive CHECK (unit_price >= 0)
);

CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product ON cart_items(product_id);
CREATE UNIQUE INDEX idx_unique_product_per_cart ON cart_items(cart_id, product_id);

-- =====================================================
-- TABLA: orders
-- =====================================================
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    shipping_address_line1 VARCHAR(255) NOT NULL,
    shipping_address_line2 VARCHAR(255),
    shipping_city VARCHAR(100) NOT NULL,
    shipping_state VARCHAR(100) NOT NULL,
    shipping_postal_code VARCHAR(20) NOT NULL,
    shipping_country VARCHAR(2) NOT NULL DEFAULT 'MX',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    subtotal NUMERIC(12, 2) NOT NULL,
    tax NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    shipping_cost NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    total_amount NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,

    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE RESTRICT,
    CONSTRAINT chk_order_status CHECK (status IN (
        'PENDING', 'PAYMENT_PROCESSING', 'PAYMENT_FAILED', 'PAID',
        'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED'
    ))
);

CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);

-- =====================================================
-- TABLA: order_items
-- =====================================================
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL GENERATED ALWAYS AS (quantity * unit_price) STORED,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE RESTRICT,
    CONSTRAINT chk_order_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);

-- =====================================================
-- TABLA: payments
-- =====================================================
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    order_id BIGINT NOT NULL,
    tokenized_card_id BIGINT NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'MXN',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    attempt_number INT NOT NULL DEFAULT 1,
    max_attempts INT NOT NULL DEFAULT 3,
    transaction_id VARCHAR(255),
    payment_gateway_response TEXT,
    failure_reason TEXT,
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payments_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_payments_tokenized_card FOREIGN KEY (tokenized_card_id)
        REFERENCES tokenized_cards(id) ON DELETE RESTRICT,
    CONSTRAINT chk_payment_status CHECK (status IN (
        'PENDING', 'PROCESSING', 'APPROVED', 'DECLINED', 'FAILED', 'REFUNDED'
    )),
    CONSTRAINT chk_attempt_number CHECK (attempt_number > 0 AND attempt_number <= max_attempts),
    CONSTRAINT chk_max_attempts CHECK (max_attempts > 0),
    CONSTRAINT chk_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_tokenized_card ON payments(tokenized_card_id);
CREATE INDEX idx_payments_created_at ON payments(created_at DESC);

-- =====================================================
-- TABLA: transaction_logs
-- =====================================================
CREATE TABLE transaction_logs (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    transaction_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    entity_uuid UUID,
    http_method VARCHAR(10),
    endpoint VARCHAR(500),
    request_body TEXT,
    response_body TEXT,
    status_code INT,
    customer_id BIGINT,
    ip_address INET,
    user_agent TEXT,
    execution_time_ms INT,
    success BOOLEAN NOT NULL DEFAULT true,
    error_message TEXT,
    stack_trace TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transaction_logs_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE SET NULL
);

CREATE INDEX idx_transaction_logs_uuid ON transaction_logs(uuid);
CREATE INDEX idx_transaction_logs_type ON transaction_logs(transaction_type);
CREATE INDEX idx_transaction_logs_entity ON transaction_logs(entity_type, entity_id);
CREATE INDEX idx_transaction_logs_customer ON transaction_logs(customer_id);
CREATE INDEX idx_transaction_logs_created_at ON transaction_logs(created_at DESC);
CREATE INDEX idx_transaction_logs_success ON transaction_logs(success) WHERE success = false;

-- =====================================================
-- TABLA: email_notifications
-- =====================================================
CREATE TABLE email_notifications (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT uuid_generate_v4() UNIQUE,
    customer_id BIGINT NOT NULL,
    order_id BIGINT,
    payment_id BIGINT,
    email_to VARCHAR(255) NOT NULL,
    email_subject VARCHAR(500) NOT NULL,
    email_body TEXT NOT NULL,
    email_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMP,
    error_message TEXT,
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 3,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_email_notifications_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_email_notifications_order FOREIGN KEY (order_id)
        REFERENCES orders(id) ON DELETE SET NULL,
    CONSTRAINT fk_email_notifications_payment FOREIGN KEY (payment_id)
        REFERENCES payments(id) ON DELETE SET NULL,
    CONSTRAINT chk_email_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'CANCELLED')),
    CONSTRAINT chk_email_type CHECK (email_type IN (
        'PAYMENT_SUCCESS', 'PAYMENT_FAILED', 'ORDER_CONFIRMATION',
        'ORDER_SHIPPED', 'ORDER_DELIVERED', 'WELCOME'
    )),
    CONSTRAINT chk_retry_count CHECK (retry_count <= max_retries)
);

CREATE INDEX idx_email_notifications_customer ON email_notifications(customer_id);
CREATE INDEX idx_email_notifications_status ON email_notifications(status);
CREATE INDEX idx_email_notifications_created_at ON email_notifications(created_at DESC);
CREATE INDEX idx_email_pending ON email_notifications(status, retry_count) WHERE status = 'PENDING';

-- =====================================================
-- TABLA: system_configuration
-- =====================================================
CREATE TABLE system_configuration (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    data_type VARCHAR(20) NOT NULL DEFAULT 'STRING',
    description TEXT,
    is_sensitive BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_data_type CHECK (data_type IN ('STRING', 'INTEGER', 'DECIMAL', 'BOOLEAN', 'JSON'))
);

CREATE INDEX idx_system_configuration_key ON system_configuration(config_key);
CREATE INDEX idx_system_configuration_active ON system_configuration(is_active) WHERE is_active = true;

-- =====================================================
-- TRIGGERS: updated_at automático
-- =====================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_customers_updated_at BEFORE UPDATE ON customers FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_tokenized_cards_updated_at BEFORE UPDATE ON tokenized_cards FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_shopping_carts_updated_at BEFORE UPDATE ON shopping_carts FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_cart_items_updated_at BEFORE UPDATE ON cart_items FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON orders FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payments FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_email_notifications_updated_at BEFORE UPDATE ON email_notifications FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_system_configuration_updated_at BEFORE UPDATE ON system_configuration FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- FUNCIONES DE NEGOCIO
-- =====================================================
CREATE OR REPLACE FUNCTION cleanup_expired_carts()
RETURNS void AS $$
BEGIN
    UPDATE shopping_carts
    SET status = 'EXPIRED'
    WHERE status = 'ACTIVE'
    AND expires_at < CURRENT_TIMESTAMP;

    DELETE FROM shopping_carts
    WHERE status = 'EXPIRED'
    AND updated_at < CURRENT_TIMESTAMP - INTERVAL '30 days';
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION merge_anonymous_cart_to_customer(
    p_session_id VARCHAR(255),
    p_customer_id BIGINT
)
RETURNS void AS $$
DECLARE
    v_anonymous_cart_id BIGINT;
    v_customer_cart_id BIGINT;
BEGIN
    SELECT id INTO v_anonymous_cart_id
    FROM shopping_carts
    WHERE session_id = p_session_id
    AND status = 'ACTIVE'
    AND customer_id IS NULL
    LIMIT 1;

    IF v_anonymous_cart_id IS NULL THEN
        RETURN;
    END IF;

    SELECT id INTO v_customer_cart_id
    FROM shopping_carts
    WHERE customer_id = p_customer_id
    AND status = 'ACTIVE'
    LIMIT 1;

    IF v_customer_cart_id IS NULL THEN
        UPDATE shopping_carts
        SET customer_id = p_customer_id,
            session_id = NULL,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = v_anonymous_cart_id;
    ELSE
        INSERT INTO cart_items (cart_id, product_id, quantity, unit_price)
        SELECT v_customer_cart_id, product_id, quantity, unit_price
        FROM cart_items
        WHERE cart_id = v_anonymous_cart_id
        ON CONFLICT (cart_id, product_id) DO UPDATE
        SET quantity = cart_items.quantity + EXCLUDED.quantity,
            updated_at = CURRENT_TIMESTAMP;

        UPDATE shopping_carts
        SET status = 'CONVERTED',
            updated_at = CURRENT_TIMESTAMP
        WHERE id = v_anonymous_cart_id;

        UPDATE shopping_carts
        SET total_items = (
                SELECT COALESCE(SUM(quantity), 0)
                FROM cart_items
                WHERE cart_id = v_customer_cart_id
            ),
            total_amount = (
                SELECT COALESCE(SUM(subtotal), 0)
                FROM cart_items
                WHERE cart_id = v_customer_cart_id
            ),
            updated_at = CURRENT_TIMESTAMP
        WHERE id = v_customer_cart_id;
    END IF;
END;
$$ LANGUAGE plpgsql;