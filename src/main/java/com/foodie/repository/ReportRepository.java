package com.foodie.repository;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReportRepository {

    private final JdbcTemplate jdbc;

    public ReportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> availableFoods() {
        return jdbc.queryForList("""
                SELECT food_id, food_name, category, price
                FROM food_items
                WHERE availability = TRUE
                ORDER BY price ASC
                """);
    }

    public List<Map<String, Object>> ordersWithCustomers() {
        return jdbc.queryForList("""
                SELECT o.order_id, c.name, o.order_date, o.status, o.total_amount
                FROM orders o
                JOIN customers c ON o.customer_id = c.customer_id
                ORDER BY o.order_date
                """);
    }

    public List<Map<String, Object>> ordersPerCustomer() {
        return jdbc.queryForList("""
                SELECT c.customer_id, c.name, COUNT(o.order_id) AS total_orders
                FROM customers c
                LEFT JOIN orders o ON c.customer_id = o.customer_id
                GROUP BY c.customer_id, c.name
                ORDER BY total_orders DESC, c.name
                """);
    }

    public List<Map<String, Object>> topSellingFoods() {
        return jdbc.queryForList("""
                SELECT f.food_name, SUM(oi.quantity) AS units_sold
                FROM food_items f
                JOIN order_items oi ON f.food_id = oi.food_id
                JOIN orders o ON oi.order_id = o.order_id
                WHERE o.status <> 'CANCELLED'
                GROUP BY f.food_id, f.food_name
                ORDER BY units_sold DESC, f.food_name
                LIMIT 5
                """);
    }

    public List<Map<String, Object>> restaurantRevenue() {
        return jdbc.queryForList("""
                SELECT r.name, SUM(oi.quantity * oi.unit_price) AS revenue
                FROM restaurants r
                JOIN food_items f ON r.restaurant_id = f.restaurant_id
                JOIN order_items oi ON f.food_id = oi.food_id
                JOIN orders o ON oi.order_id = o.order_id
                WHERE o.status <> 'CANCELLED'
                GROUP BY r.restaurant_id, r.name
                ORDER BY revenue DESC
                """);
    }

    public List<Map<String, Object>> aboveAverageCustomers() {
        return jdbc.queryForList("""
                SELECT customer_id, name
                FROM customers
                WHERE customer_id IN (
                    SELECT customer_id
                    FROM orders
                    GROUP BY customer_id
                    HAVING AVG(total_amount) > (SELECT AVG(total_amount) FROM orders)
                )
                """);
    }

    public List<Map<String, Object>> monthlyStats() {
        return jdbc.queryForList("""
                SELECT DATE_FORMAT(order_date, '%Y-%m') AS month,
                       COUNT(*) AS total_orders,
                       SUM(total_amount) AS order_value
                FROM orders
                WHERE status <> 'CANCELLED'
                GROUP BY DATE_FORMAT(order_date, '%Y-%m')
                ORDER BY month
                """);
    }

    public List<Map<String, Object>> restaurantsWithManyDishes() {
        return jdbc.queryForList("""
                SELECT r.name, COUNT(f.food_id) AS dish_count
                FROM restaurants r
                JOIN food_items f ON r.restaurant_id = f.restaurant_id
                GROUP BY r.restaurant_id, r.name
                HAVING COUNT(f.food_id) > 3
                """);
    }
}
