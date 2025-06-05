CREATE TABLE todo
(
    id          SERIAL PRIMARY KEY,
    title       TEXT    NOT NULL,
    description TEXT    NOT NULL,
    done        BOOLEAN NOT NULL
)