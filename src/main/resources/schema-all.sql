DROP TABLE transactions IF EXISTS;

CREATE TABLE transactions  (
    id BIGINT IDENTITY NOT NULL PRIMARY KEY,
	reference INT, 
	account_number VARCHAR(20), 
	description VARCHAR(200), 
	start_balance DECIMAL(20, 2), 
	mutation DECIMAL(20, 2), 
	end_balance DECIMAL(20, 2)
);