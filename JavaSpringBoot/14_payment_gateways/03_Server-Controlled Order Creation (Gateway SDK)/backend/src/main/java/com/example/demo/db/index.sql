CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_active_deleted (is_deleted, is_active)
);

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) 
        REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE user_profiles (
    user_id BIGINT PRIMARY KEY, -- Fixed: Removed AUTO_INCREMENT
    profile_media_id BIGINT,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    bio VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
        , foreign key (profile_media_id) REFERENCES media_assets(id)
);

CREATE TABLE user_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(128) NOT NULL,
    refresh_token TEXT DEFAULT NULL,
    ip_address VARCHAR(45) DEFAULT NULL, -- Fixed: IPv6 safe length
    user_agent TEXT DEFAULT NULL,
    device_name VARCHAR(255) DEFAULT NULL,
    login_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_activity DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at DATETIME DEFAULT NULL,
    UNIQUE KEY uk_session_id (session_id),
    CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE
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


CREATE TABLE notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uploader_id BIGINT NOT NULL,                      -- Creator/Admin who uploaded the notes
    media_asset_id BIGINT NOT NULL UNIQUE,            -- Points to the PDF/DOCX file in media_assets
    title VARCHAR(255) NOT NULL,
    description TEXT DEFAULT NULL,
    price_in_subunits BIGINT NOT NULL DEFAULT 0,      -- e.g., 29900 = ₹299.00
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    is_published BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_notes_uploader FOREIGN KEY (uploader_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notes_media FOREIGN KEY (media_asset_id) 
        REFERENCES media_assets(id) ON DELETE CASCADE,

    INDEX idx_published_notes (is_published, is_deleted)
);