CREATE TABLE Products (
    product_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    unit_price DECIMAL(10, 2) NOT NULL
);
CREATE TABLE Inventory (
    product_id INT REFERENCES Products(product_id),
    quantity INT NOT NULL CHECK (quantity >= 0),
    PRIMARY KEY (product_id)
);
CREATE TABLE Transactions (
    transaction_id SERIAL PRIMARY KEY,
    product_id INT REFERENCES Products(product_id),
    transaction_type VARCHAR(10) CHECK (transaction_type IN ('compra', 'venda')),
    quantity INT NOT NULL CHECK (quantity > 0),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE OR REPLACE PROCEDURE AddToInventory(p_product_id INT, p_quantity INT)
LANGUAGE plpgsql
AS $$
BEGIN
   
    UPDATE Inventory
    SET quantity = quantity + p_quantity
    WHERE product_id = p_product_id;

    
    INSERT INTO Transactions (product_id, transaction_type, quantity)
    VALUES (p_product_id, 'compra', p_quantity);
    
    
    IF NOT FOUND THEN
        INSERT INTO Inventory (product_id, quantity)
        VALUES (p_product_id, p_quantity);
    END IF;
END;
$$;

CREATE OR REPLACE PROCEDURE SellProduct(p_product_id INT, p_quantity INT)
LANGUAGE plpgsql
AS $$
BEGIN
    
    IF (SELECT quantity FROM Inventory WHERE product_id = p_product_id) < p_quantity THEN
        RAISE EXCEPTION 'Estoque insuficiente para o produto %', p_product_id;
    END IF;

    
    UPDATE Inventory
    SET quantity = quantity - p_quantity
    WHERE product_id = p_product_id;

    
    INSERT INTO Transactions (product_id, transaction_type, quantity)
    VALUES (p_product_id, 'venda', p_quantity);
END;
$$;

CREATE OR REPLACE FUNCTION GetStockLevel(p_product_id INT)
RETURNS INT
LANGUAGE plpgsql
AS $$
DECLARE 
	stock_level INT;
BEGIN
	SELECT Quantity INTO stock_level
	FROM Inventory
	WHERE  product_id = p_product_id;

	RETURN stock_level;
END;
$$;

CALL AddToInventory(1, 100);

SELECT * FROM Inventory WHERE product_id = 1;

CALL SellProduct(1, 50);

SELECT * FROM Inventory WHERE product_id = 1;

SELECT * FROM GetStockLevel (1) AS Quantidade


