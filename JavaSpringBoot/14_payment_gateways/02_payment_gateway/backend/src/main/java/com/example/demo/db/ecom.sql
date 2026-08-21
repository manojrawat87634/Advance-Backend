
-- 1. ITEMS / PRODUCTS TABLE
CREATE TABLE items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price  DECIMAL(10, 2) NOT NULL, -- Price in PAISE (e.g., ₹500 = 50000)
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_items_active_deleted (is_deleted, is_active)
);

-- 2. ORDERS TABLE
-- Tracks the order creation phase before sending the request to Razorpay
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,                  -- WHO created the order
    item_id BIGINT NOT NULL,                  -- WHAT item is being purchased
        amount DECIMAL(10, 2) NOT NULL,       -- Amount paid in RUPEES
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    status ENUM('CREATED', 'PAID', 'FAILED', 'EXPIRED') NOT NULL DEFAULT 'CREATED',
    razorpay_order_id VARCHAR(255) UNIQUE NOT NULL, -- Order ID returned by Razorpay API (order_xxx)
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_orders_item FOREIGN KEY (item_id) 
        REFERENCES items(id)
);

-- Indexes for fast order lookups by user and status
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_orders_razorpay_id ON orders(razorpay_order_id);


-- 3. PAYMENTS TABLE
-- Tracks the payment verification phase after checkout completion
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,                 -- Associated Order ID
    user_id BIGINT NOT NULL,                  -- WHO made the payment
    amount DECIMAL(10, 2) NOT NULL,                  -- Amount paid in PAISE
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    method VARCHAR(50),                       -- e.g., 'UPI', 'CARD', 'NETBANKING'
    status ENUM('SUCCESS', 'FAILED', 'PENDING', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    
    -- Razorpay Verification Fields
    razorpay_payment_id VARCHAR(255) UNIQUE NOT NULL, -- Payment ID from Modal (pay_xxx)
    razorpay_signature VARCHAR(255) NOT NULL,          -- Signature verified on backend
    
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) 
        REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_payments_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for rapid payment queries and reporting
CREATE INDEX idx_payments_user ON payments(user_id, status);
CREATE INDEX idx_payments_order ON payments(order_id);