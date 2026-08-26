CREATE TABLE webhook_logs (
    id BIGSERIAL PRIMARY KEY,
    
    -- Gateway's unique event identifier (e.g., 'evt_3MvwE1LkdIwJyWB31' or 'event_L3k9xZ812')
    event_id VARCHAR(128) NOT NULL UNIQUE,  
    
    gateway_name VARCHAR(50) NOT NULL,          -- 'STRIPE', 'RAZORPAY', 'PAYPAL'
    event_type VARCHAR(100) NOT NULL,          -- 'payment_intent.succeeded', 'order.paid'
    
    status VARCHAR(30) NOT NULL DEFAULT 'PROCESSED', -- 'PROCESSED', 'FAILED', 'IGNORED'
    
    processed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Fast primary lookup index on event_id for instant deduplication checks
CREATE INDEX idx_webhook_logs_event_id ON webhook_logs(event_id);