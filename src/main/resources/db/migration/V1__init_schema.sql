CREATE TABLE franchises (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL
);

CREATE TABLE branches (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(150) NOT NULL,
    franchise_id BIGINT NOT NULL REFERENCES franchises (id) ON DELETE CASCADE
);

CREATE INDEX idx_branches_franchise_id ON branches (franchise_id);

CREATE TABLE products (
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(150) NOT NULL,
    stock     INTEGER NOT NULL CHECK (stock >= 0),
    branch_id BIGINT NOT NULL REFERENCES branches (id) ON DELETE CASCADE
);

CREATE INDEX idx_products_branch_id ON products (branch_id);
CREATE INDEX idx_products_branch_id_stock ON products (branch_id, stock DESC);
