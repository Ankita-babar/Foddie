USE foodie;

-- Q1: available food items sorted by price
SELECT food_id, food_name, category, price
FROM food_items
WHERE availability = TRUE
ORDER BY price ASC;

-- Q2: orders with customer details (INNER JOIN)
SELECT o.order_id, c.name, o.order_date, o.status, o.total_amount
FROM orders o
JOIN customers c ON o.customer_id = c.customer_id
ORDER BY o.order_date;

-- Q3: total orders per customer (LEFT JOIN, COUNT, GROUP BY)
SELECT c.customer_id, c.name, COUNT(o.order_id) AS total_orders
FROM customers c
LEFT JOIN orders o ON c.customer_id = o.customer_id
GROUP BY c.customer_id, c.name
ORDER BY total_orders DESC, c.name;

-- Q4: top 5 best-selling food items
SELECT f.food_name, SUM(oi.quantity) AS units_sold
FROM food_items f
JOIN order_items oi ON f.food_id = oi.food_id
JOIN orders o ON oi.order_id = o.order_id
WHERE o.status <> 'CANCELLED'
GROUP BY f.food_id, f.food_name
ORDER BY units_sold DESC, f.food_name
LIMIT 5;

-- Q5: revenue by restaurant
SELECT r.name, SUM(oi.quantity * oi.unit_price) AS revenue
FROM restaurants r
JOIN food_items f ON r.restaurant_id = f.restaurant_id
JOIN order_items oi ON f.food_id = oi.food_id
JOIN orders o ON oi.order_id = o.order_id
WHERE o.status <> 'CANCELLED'
GROUP BY r.restaurant_id, r.name
ORDER BY revenue DESC;

-- Q6: customers whose average order value is above the overall average (nested query, HAVING)
SELECT customer_id, name
FROM customers
WHERE customer_id IN (
    SELECT customer_id
    FROM orders
    GROUP BY customer_id
    HAVING AVG(total_amount) > (SELECT AVG(total_amount) FROM orders)
);

-- Q7: monthly order statistics
SELECT DATE_FORMAT(order_date, '%Y-%m') AS month,
       COUNT(*) AS total_orders,
       SUM(total_amount) AS order_value
FROM orders
WHERE status <> 'CANCELLED'
GROUP BY DATE_FORMAT(order_date, '%Y-%m')
ORDER BY month;

-- Q8: restaurants offering more than three dishes
SELECT r.name, COUNT(f.food_id) AS dish_count
FROM restaurants r
JOIN food_items f ON r.restaurant_id = f.restaurant_id
GROUP BY r.restaurant_id, r.name
HAVING COUNT(f.food_id) > 3;

-- Index verification
EXPLAIN SELECT * FROM orders WHERE customer_id = 5;
EXPLAIN SELECT * FROM orders WHERE order_date BETWEEN '2026-09-01' AND '2026-09-30';
