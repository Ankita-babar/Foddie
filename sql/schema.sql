CREATE DATABASE IF NOT EXISTS foodie;
USE foodie;

DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS food_items;
DROP TABLE IF EXISTS restaurants;
DROP TABLE IF EXISTS customers;

CREATE TABLE customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(120) NOT NULL UNIQUE,
    phone       VARCHAR(15)  NOT NULL,
    address     VARCHAR(255) NOT NULL
);

CREATE TABLE restaurants (
    restaurant_id INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    address       VARCHAR(255) NOT NULL,
    phone         VARCHAR(15)  NOT NULL,
    rating        DECIMAL(2,1) NOT NULL DEFAULT 0.0,
    CONSTRAINT chk_rating CHECK (rating BETWEEN 0 AND 5)
);

CREATE TABLE food_items (
    food_id       INT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id INT NOT NULL,
    food_name     VARCHAR(100) NOT NULL,
    category      VARCHAR(50)  NOT NULL,
    price         DECIMAL(8,2) NOT NULL,
    availability  BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_price CHECK (price > 0),
    CONSTRAINT fk_food_restaurant FOREIGN KEY (restaurant_id)
        REFERENCES restaurants(restaurant_id) ON DELETE CASCADE
);

CREATE TABLE orders (
    order_id     INT AUTO_INCREMENT PRIMARY KEY,
    customer_id  INT NOT NULL,
    order_date   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status       ENUM('PLACED','PREPARING','OUT_FOR_DELIVERY','DELIVERED','CANCELLED') NOT NULL DEFAULT 'PLACED',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id) ON DELETE RESTRICT
);

CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id      INT NOT NULL,
    food_id       INT NOT NULL,
    quantity      INT NOT NULL,
    unit_price    DECIMAL(8,2) NOT NULL,
    CONSTRAINT chk_quantity CHECK (quantity > 0),
    CONSTRAINT uq_order_food UNIQUE (order_id, food_id),
    CONSTRAINT fk_items_order FOREIGN KEY (order_id)
        REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_items_food FOREIGN KEY (food_id)
        REFERENCES food_items(food_id) ON DELETE RESTRICT
);

CREATE TABLE payments (
    payment_id     INT AUTO_INCREMENT PRIMARY KEY,
    order_id       INT NOT NULL UNIQUE,
    payment_method ENUM('CARD','UPI','COD') NOT NULL,
    payment_status ENUM('PENDING','PAID','FAILED','REFUNDED') NOT NULL DEFAULT 'PENDING',
    amount         DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id)
        REFERENCES orders(order_id) ON DELETE CASCADE
);

CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_date ON orders(order_date);
CREATE INDEX idx_food_category ON food_items(category);
