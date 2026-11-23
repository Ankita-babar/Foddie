package com.foodie.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.foodie.model.FoodOrder;

@Repository
public class OrderRepository {

    private static final String SELECT = """
            SELECT o.order_id, o.customer_id, c.name AS customer_name,
                   o.order_date, o.status, o.total_amount
            FROM orders o
            JOIN customers c ON o.customer_id = c.customer_id
            """;

    private final JdbcTemplate jdbc;

    private final RowMapper<FoodOrder> mapper = (rs, n) -> new FoodOrder(
            rs.getInt("order_id"),
            rs.getInt("customer_id"),
            rs.getString("customer_name"),
            rs.getTimestamp("order_date").toLocalDateTime(),
            rs.getString("status"),
            rs.getBigDecimal("total_amount"));

    public OrderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<FoodOrder> findAll() {
        return jdbc.query(SELECT + " ORDER BY o.order_id DESC", mapper);
    }

    public List<FoodOrder> findByCustomer(int customerId) {
        return jdbc.query(SELECT + " WHERE o.customer_id = ? ORDER BY o.order_date DESC", mapper, customerId);
    }

    public Optional<FoodOrder> findById(int id) {
        return jdbc.query(SELECT + " WHERE o.order_id = ?", mapper, id).stream().findFirst();
    }

    public int create(int customerId) {
        return SqlHelper.insert(jdbc, "INSERT INTO orders (customer_id) VALUES (?)", customerId);
    }

    public int updateStatus(int id, String status) {
        return jdbc.update("UPDATE orders SET status = ? WHERE order_id = ?", status, id);
    }

    public void recalculateTotal(int id) {
        jdbc.update("""
                UPDATE orders
                SET total_amount = (SELECT COALESCE(SUM(quantity * unit_price), 0)
                                    FROM order_items WHERE order_id = ?)
                WHERE order_id = ?
                """, id, id);
    }

    public int delete(int id) {
        return jdbc.update("DELETE FROM orders WHERE order_id = ?", id);
    }
}
