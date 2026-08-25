CREATE TABLE payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    
    -- Links back to the parent order
    payment_order_id BIGINT NOT NULL,
    
    -- Gateway Identifiers
    gateway_name VARCHAR(50) NOT NULL,            -- 'RAZORPAY', 'STRIPE', 'PAYPAL'
    transaction_id VARCHAR(128) NOT NULL UNIQUE,  -- Gateway Payment ID (e.g., 'pay_Kx987123A')
    
    -- Event Classification
    transaction_type VARCHAR(30) NOT NULL,        -- 'AUTHORIZATION', 'CAPTURE', 'REFUND', 'FAILED_ATTEMPT'
    status VARCHAR(30) NOT NULL,                  -- 'SUCCESS', 'FAILED', 'PENDING'
    
    -- Financial Tracking
    amount DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    gateway_fee DECIMAL(10, 2) DEFAULT 0.00,       -- Fee charged by gateway (e.g., 2%)
    tax DECIMAL(10, 2) DEFAULT 0.00,               -- GST/VAT applied by gateway
    
    -- Raw Audit Log (Stores the exact raw JSON string sent by gateway)
    raw_response JSONB, 
    
    -- Timestamp (Append-Only)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Foreign Key
    CONSTRAINT fk_transactions_order FOREIGN KEY (payment_order_id) REFERENCES payment_orders(id) ON DELETE RESTRICT
);

-- Indexing for fast financial reporting & reconciliation
CREATE INDEX idx_transactions_order_id ON payment_transactions(payment_order_id);
CREATE INDEX idx_transactions_gateway_tx_id ON payment_transactions(transaction_id);