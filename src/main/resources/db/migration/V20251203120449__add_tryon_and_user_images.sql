-----------------------
-- USER_IMAGES
-----------------------
CREATE TABLE IF NOT EXISTS user_images (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                           user_id UUID NOT NULL,
                                           image_url TEXT NOT NULL,
                                           file_name VARCHAR(255),
                                           file_size INT,
                                           width INT,
                                           height INT,
                                           format VARCHAR(20),
                                           is_primary BOOLEAN DEFAULT FALSE,
                                           source VARCHAR(50) DEFAULT 'TRYON',
                                           meta JSONB,
                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                           CONSTRAINT fk_user_images_user
                                               FOREIGN KEY (user_id)
                                                   REFERENCES users(id)
                                                   ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_images_user ON user_images(user_id);
CREATE INDEX IF NOT EXISTS idx_user_images_created ON user_images(created_at);

-----------------------
-- TRYON_SESSIONS
-----------------------
ALTER TABLE tryon_sessions
    ADD COLUMN IF NOT EXISTS input_image_id UUID,
    ADD COLUMN IF NOT EXISTS model_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS prompt TEXT,
    ADD COLUMN IF NOT EXISTS request_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS estimated_cost DECIMAL(10,4),
    ADD COLUMN IF NOT EXISTS finished_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS fail_reason TEXT,
    ADD COLUMN IF NOT EXISTS retry_count INT DEFAULT 0;

DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.table_constraints
            WHERE constraint_name = 'fk_tryon_input_image'
        ) THEN
            ALTER TABLE tryon_sessions
                ADD CONSTRAINT fk_tryon_input_image
                    FOREIGN KEY (input_image_id)
                        REFERENCES user_images(id)
                        ON DELETE SET NULL;
        END IF;
    END$$;

CREATE INDEX IF NOT EXISTS idx_tryon_user ON tryon_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_tryon_product ON tryon_sessions(selected_product_id);
CREATE INDEX IF NOT EXISTS idx_tryon_status ON tryon_sessions(status);
CREATE INDEX IF NOT EXISTS idx_tryon_request ON tryon_sessions(request_id);
CREATE INDEX IF NOT EXISTS idx_tryon_created ON tryon_sessions(created_at);