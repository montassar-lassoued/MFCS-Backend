

CREATE TABLE Journal (
    id BIGINT IDENTITY PRIMARY KEY,
    controller VARCHAR(255),
    _date date,
    content VARBINARY(MAX)
);