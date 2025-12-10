CREATE TABLE tags (
                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                      name VARCHAR(100) NOT NULL UNIQUE,      -- ví dụ: MEN, WOMEN, KIDS
                      display_name VARCHAR(150),             -- ví dụ: Nam, Nữ, Trẻ em
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE product_tags (
                              product_id UUID NOT NULL,
                              tag_id UUID NOT NULL,
                              PRIMARY KEY (product_id, tag_id),
                              CONSTRAINT fk_product_tags_product
                                  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                              CONSTRAINT fk_product_tags_tag
                                  FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

CREATE INDEX idx_product_tags_product ON product_tags(product_id);
CREATE INDEX idx_product_tags_tag ON product_tags(tag_id);