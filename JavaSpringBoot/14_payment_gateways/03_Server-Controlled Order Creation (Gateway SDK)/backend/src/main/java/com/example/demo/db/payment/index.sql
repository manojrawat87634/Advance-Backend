CREATE TABLE payment_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_reference_id VARCHAR(64) NOT NULL UNIQUE,
    gateway_name VARCHAR(30) NOT NULL DEFAULT 'RAZORPAY',
    gateway_order_id VARCHAR(255) UNIQUE DEFAULT NULL,
    amount_in_subunits BIGINT NOT NULL,                 -- e.g., 50000 (Integer, no decimals)
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
    gateway_payment_id VARCHAR(255) NOT NULL UNIQUE, -- Unique payment ID from gateway (e.g., pay_Hx8z...)
    gateway_signature VARCHAR(500) DEFAULT NULL,      -- HMAC signature string for verification
    payment_method VARCHAR(50) DEFAULT NULL,          -- 'UPI', 'CARD', 'NETBANKING', etc.
    amount_in_subunits BIGINT NOT NULL,               -- Stored as subunits (e.g., 50000 = ₹500.00)
    status ENUM('SUCCESS', 'FAILED', 'REFUNDED') NOT NULL,
    failure_reason TEXT DEFAULT NULL,                 -- Populated if payment failed on gateway side
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_payment_transactions_order FOREIGN KEY (payment_order_id) 
        REFERENCES payment_orders(id) ON DELETE CASCADE,
    INDEX idx_order_transactions (payment_order_id, status)
);

CREATE TABLE user_entitlements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    media_asset_id BIGINT NOT NULL,               -- Direct link to the purchased PDF in media_assets
    granted_by_transaction_id BIGINT DEFAULT NULL, -- Direct link to the successful payment transaction
    
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at DATETIME DEFAULT NULL,              -- NULL = Lifetime access
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign Keys enforcing data integrity
    CONSTRAINT fk_entitlement_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_entitlement_media FOREIGN KEY (media_asset_id) 
        REFERENCES media_assets(id) ON DELETE CASCADE,
    CONSTRAINT fk_entitlement_transaction FOREIGN KEY (granted_by_transaction_id) 
        REFERENCES payment_transactions(id) ON DELETE SET NULL,
        
    -- Prevents buying the exact same PDF multiple times
    UNIQUE KEY uk_user_media (user_id, media_asset_id),
    INDEX idx_user_media_active (user_id, media_asset_id, is_active)
);

CREATE TABLE webhook_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Idempotency & Gateway Info
    event_id VARCHAR(255) NOT NULL UNIQUE,             -- Unique event identifier from gateway (e.g., evt_1N2x...)
    gateway_name VARCHAR(30) NOT NULL DEFAULT 'RAZORPAY',-- Gateway source ('RAZORPAY', 'STRIPE', etc.)
    event_type VARCHAR(100) NOT NULL,                  -- Event category (e.g., 'payment.captured', 'payment.failed')
    
    -- Processing Lifecycle & Status
    status ENUM('PENDING', 'PROCESSED', 'FAILED', 'IGNORED') NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,                -- Tracks execution retries if processing fails internally
    error_message TEXT DEFAULT NULL,                   -- Stores stack trace or error reason if status = 'FAILED'
    
    -- Full Raw Request Payload
    payload JSON NOT NULL,                             -- Unmodified raw webhook JSON payload received from gateway
    
    -- Audit Timestamps
    processed_at DATETIME DEFAULT NULL,                -- Timestamp when entitlement/order update successfully completed
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes for speed and debugging
    INDEX idx_event_lookup (event_id, status),
    INDEX idx_event_type_status (event_type, status)
);