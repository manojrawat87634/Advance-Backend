CREATE TABLE payment_orders (
    id BIGSERIAL PRIMARY KEY,    
    -- Internal Identifiers
    order_reference_id VARCHAR(64) NOT NULL UNIQUE,  -- e.g., 'ORD-2026-X981A' (Internal UUID)
    user_id BIGINT NOT NULL,                        -- ID of the user purchasing
    product_id VARCHAR(100) NOT NULL,               -- Product / Service SKU being bought
    -- Gateway Identifiers
    gateway_name VARCHAR(30) NOT NULL,              -- 'RAZORPAY', 'STRIPE', etc.
    gateway_order_id VARCHAR(255) UNIQUE,           -- ID returned by Gateway API (e.g., 'order_EKwxwAgItmmXdp')
    
    -- Financial Parameters
    amount DECIMAL(12, 2) NOT NULL,                 -- Total price locked by backend
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',     -- ISO 4217 Currency Code
    
    -- State Machine Tracking
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',  -- 'CREATED', 'PENDING', 'PAID', 'FAILED', 'REFUNDED'
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Key Constraint (Assuming users table exists)
    CONSTRAINT fk_payment_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_payment_orders_user_status ON payment_orders(user_id, status);
CREATE INDEX idx_payment_orders_gateway_id ON payment_orders(gateway_order_id);