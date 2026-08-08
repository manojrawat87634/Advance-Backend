-- 1. USERS TABLE
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    is_email_verified TINYINT(1) NOT NULL DEFAULT 0,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. USER PROFILES TABLE (Clean, no circular media references)
CREATE TABLE user_profiles (
    user_id BIGINT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    bio TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_user_profiles_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);

-- 3. MEDIA ASSETS TABLE (Polymorphic, Soft-Deletable, Multi-Tenant Ready)
CREATE TABLE media_assets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Ownership & Application Context
    owner_id BIGINT NOT NULL,              -- User ID extracted from JWT ('sub' claim)
    client_app_id VARCHAR(100) NOT NULL,   -- e.g., 'E-Commerce-App', 'CRM-Software'
    
    -- Domain Mapping
    entity_type VARCHAR(50) NOT NULL,      -- e.g., 'PROFILE_AVATAR', 'PRODUCT_IMAGE'
    entity_id VARCHAR(255) NOT NULL,       -- e.g., '1' (User ID) or UUID
    
    -- Object Storage Metadata
    file_key VARCHAR(500) NOT NULL UNIQUE, -- MinIO/S3 path
    file_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    
    -- Access Control & Lifecycle
    visibility ENUM('PRIVATE', 'APP_RESTRICTED', 'PUBLIC_READ') NOT NULL DEFAULT 'PRIVATE',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign Key
    CONSTRAINT fk_media_assets_owner 
        FOREIGN KEY (owner_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);

-- INDEXES FOR OPTIMIZED PERFORMANCE
CREATE INDEX idx_owner_app ON media_assets(owner_id, client_app_id);
CREATE INDEX idx_entity_lookup ON media_assets(entity_type, entity_id, is_deleted);
CREATE INDEX idx_owner_visibility ON media_assets(owner_id, visibility, is_deleted);