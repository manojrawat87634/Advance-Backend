CREATE TABLE media_assets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- WHO owns it?
    owner_id BIGINT NOT NULL,              -- User ID extracted from JWT ('sub' claim)
    
    -- WHICH application created it?
    client_app_id VARCHAR(100),         -- e.g., 'E-Commerce-App', 'CRM-Software'
    
    -- WHAT is the context/domain?
    entity_type VARCHAR(100),           -- e.g., 'AVATAR', 'PRODUCT_IMAGE'
    entity_id VARCHAR(255),              -- e.g., 'product_9876' or 'user_4321'
    
    -- WHERE is it in S3 / R2?
    file_key VARCHAR(500) NOT NULL UNIQUE, -- S3 key path
    
    -- File Metadata
    file_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT,
    
    -- Access Control
    visibility VARCHAR(20) DEFAULT 'PRIVATE', -- 'PRIVATE', 'APP_RESTRICTED', 'PUBLIC_READ'
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint with defined delete behavior
    CONSTRAINT fk_media_assets_owner 
        FOREIGN KEY (owner_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);

-- Indexes for frequent queries
CREATE INDEX idx_owner_app ON media_assets(owner_id, client_app_id);
CREATE INDEX idx_entity ON media_assets(entity_type, entity_id);
CREATE INDEX idx_owner_visibility ON media_assets(owner_id, visibility);