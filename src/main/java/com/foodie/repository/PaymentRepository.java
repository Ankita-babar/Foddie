package com.foodie.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.foodie.model.Payment;

@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Payment> mapper = (rs, n) -> new Payment(
            rs.getInt("payment_id"),
            rs.getInt("order_id"),
            rs.getString("payment_method"),
            rs.getString("payment_status"),
            rs.getBigDecimal("amount"));

    public PaymentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Payment> findAll() {
        return jdbc.query("SELECT * FROM payments ORDER BY payment_id", mapper);
    }

    public Optional<Payment> findById(int id) {
        return jdbc.query("SELECT * FROM payments WHERE payment_id = ?", mapper, id).stream().findFirst();
    }

    public Optional<Payment> findByOrder(int orderId) {
        return jdbc.query("SELECT * FROM payments WHERE order_id = ?", mapper, orderId).stream().findFirst();
    }

    public int create(int orderId, String method, String status, BigDecimal amount) {
        return SqlHelper.insert(jdbc,
                "INSERT INTO payments (order_id, payment_method, payment_status, amount) VALUES (?, ?, ?, ?)",
                orderId, method, status, amount);
    }

    public int updateStatus(int id, String status) {
        return jdbc.update("UPDATE payments SET payment_status = ? WHERE payment_id = ?", status, id);
    }

    public void syncAmount(int orderId) {
        jdbc.update("""
                UPDATE payments
                SET amount = (SELECT total_amount FROM orders WHERE order_id = ?)
                WHERE order_id = ?
                """, orderId, orderId);
    }

    public int delete(int id) {
        return jdbc.update("DELETE FROM payments WHERE payment_id = ?", id);
    }
}
