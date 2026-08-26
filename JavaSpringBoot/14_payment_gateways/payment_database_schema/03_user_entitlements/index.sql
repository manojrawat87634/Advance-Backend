CREATE TABLE resource_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL  -- 'MOVIE', 'COURSE', 'SOFTWARE_TIER', 'BUNDLE'
);


CREATE TABLE purchasable_items (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(100) UNIQUE NOT NULL,       -- e.g., 'ITEM_MOVIE_INCEPTION', 'ITEM_COURSE_JAVA_101'
    name VARCHAR(255) NOT NULL,              -- 'Inception (4K Rent)', 'Complete Java Masterclass'
    resource_type_id INT NOT NULL REFERENCES resource_types(id),
    resource_id VARCHAR(100) NOT NULL,       -- Internal ID in Movie service ("movie_992") or Course service ("course_55")
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE user_entitlements (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL REFERENCES purchasable_items(id),
    
    payment_order_id BIGINT REFERENCES payment_orders(id),
    
    granted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NULL, -- NULL = Lifetime (e.g., Course); 48 Hours = (e.g., Movie Rental)
    is_active BOOLEAN DEFAULT TRUE,

    CONSTRAINT uk_user_item UNIQUE(user_id, item_id)
);

CREATE INDEX idx_user_entitlements_access ON user_entitlements(user_id, item_id, is_active);