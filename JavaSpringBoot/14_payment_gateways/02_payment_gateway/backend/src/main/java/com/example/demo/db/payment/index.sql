CREATE TABLE user_entitlements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    feature_key VARCHAR(100) NOT NULL,            -- e.g., 'SECRET_DOCUMENT_ACCESS'
    granted_by_payment_id BIGINT DEFAULT NULL,    -- Links back to transaction audit
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at DATETIME DEFAULT NULL,             -- NULL = Lifetime / One-Time purchase
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_entitlement_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_feature (user_id, feature_key),
    INDEX idx_user_feature_active (user_id, feature_key, is_active)
);


CREATE TABLE payment_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_reference_id VARCHAR(64) NOT NULL UNIQUE, -- Internal idempotent UUID
    gateway_name VARCHAR(30) NOT NULL,             -- 'RAZORPAY', 'STRIPE', etc.
    gateway_order_id VARCHAR(255) NOT NULL UNIQUE, -- Razorpay/Stripe order_id
    amount DECIMAL(10,2) NOT NULL,                 -- Stored directly in Rupees
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    status ENUM('PENDING', 'PAID', 'FAILED', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_payment_orders_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_orders (user_id, status)
);

CREATE TABLE payment_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_order_id BIGINT NOT NULL,
    gateway_payment_id VARCHAR(255) NOT NULL UNIQUE, -- Unique payment attempt ID from gateway
    gateway_signature VARCHAR(500) DEFAULT NULL,      -- HMAC signature string for crypt-verification
    payment_method VARCHAR(50) DEFAULT NULL,          -- 'UPI', 'CARD', etc.
    amount DECIMAL(10,2) NOT NULL,
    status ENUM('SUCCESS', 'FAILED', 'REFUNDED') NOT NULL,
    failure_reason TEXT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_payment_transactions_order FOREIGN KEY (payment_order_id) 
        REFERENCES payment_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_entitlement_grant FOREIGN KEY (payment_order_id) 
        REFERENCES payment_orders(id),
    INDEX idx_order_transactions (payment_order_id, status)
);

CREATE TABLE webhook_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(255) NOT NULL UNIQUE,         -- Gateway event ID (e.g., event_01H...)
    event_type VARCHAR(100) NOT NULL,              -- e.g., 'payment.captured'
    gateway VARCHAR(30) NOT NULL,                  -- 'RAZORPAY'
    payload JSON NOT NULL,                         -- Raw webhook body
    status ENUM('PENDING', 'PROCESSED', 'FAILED', 'DUPLICATE') NOT NULL DEFAULT 'PENDING',
    processed_at DATETIME DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_webhook_status (event_id, status)
);