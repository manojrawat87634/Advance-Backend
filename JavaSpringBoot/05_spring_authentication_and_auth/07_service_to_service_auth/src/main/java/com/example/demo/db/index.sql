CREATE TABLE media_metadata (
    id VARCHAR(255) PRIMARY KEY,          -- e.g., "med_998877"
    owner_id VARCHAR(255) NOT NULL,        -- User ID who owns the file
    storage_key VARCHAR(512) NOT NULL,     -- S3 Object key (e.g., "uploads/usr_123/avatar.png")
    mime_type VARCHAR(100) NOT NULL,       -- e.g., "image/png"
    size_bytes BIGINT NOT NULL,            -- File size in bytes
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);