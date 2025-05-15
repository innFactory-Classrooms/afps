CREATE TABLE category
(
    id   SERIAL PRIMARY KEY,
    name TEXT NOT NULL
);

ALTER TABLE todo
    ADD COLUMN category_id INT REFERENCES category (id);