package com.foodie.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.foodie.model.OrderItem;

@Repository
public class OrderItemRepository {

    private static final String SELECT = """
            SELECT oi.order_item_id, oi.order_id, oi.food_id, f.food_name, oi.quantity, oi.unit_price
            FROM order_items oi
            JOIN food_items f ON oi.food_id = f.food_id
            """;

    private final JdbcTemplate jdbc;

    private final RowMapper<OrderItem> mapper = (rs, n) -> new OrderItem(
            rs.getInt("order_item_id"),
            rs.getInt("order_id"),
            rs.getInt("food_id"),
            rs.getString("food_name"),
            rs.getInt("quantity"),
            rs.getBigDecimal("unit_price"));

    public OrderItemRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<OrderItem> findAll() {
        return jdbc.query(SELECT + " ORDER BY oi.order_item_id", mapper);
    }

    public List<OrderItem> findByOrder(int orderId) {
        return jdbc.query(SELECT + " WHERE oi.order_id = ? ORDER BY oi.order_item_id", mapper, orderId);
    }

    public Optional<OrderItem> findById(int id) {
        return jdbc.query(SELECT + " WHERE oi.order_item_id = ?", mapper, id).stream().findFirst();
    }

    public void addOrIncrease(int orderId, int foodId, int quantity, BigDecimal unitPrice) {
        jdbc.update("""
                INSERT INTO order_items (order_id, food_id, quantity, unit_price)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE quantity = quantity + ?
                """, orderId, foodId, quantity, unitPrice, quantity);
    }

    public int updateQuantity(int id, int quantity) {
        return jdbc.update("UPDATE order_items SET quantity = ? WHERE order_item_id = ?", quantity, id);
    }

    public int delete(int id) {
        return jdbc.update("DELETE FROM order_items WHERE order_item_id = ?", id);
    }
}
