ALTER TABLE tryon_sessions
    DROP COLUMN selected_product_id;

ALTER TABLE tryon_sessions
    ADD COLUMN product_image_id UUID;

ALTER TABLE tryon_sessions
    ADD CONSTRAINT fk_tryon_product_image
        FOREIGN KEY (product_image_id)
            REFERENCES product_images(id)
            ON DELETE SET NULL;

CREATE INDEX idx_tryon_product_image
    ON tryon_sessions(product_image_id);