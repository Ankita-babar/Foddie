package com.foodie.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.foodie.model.Customer;

@Repository
public class CustomerRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Customer> mapper = (rs, n) -> new Customer(
            rs.getInt("customer_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("address"));

    public CustomerRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Customer> findAll() {
        return jdbc.query("SELECT * FROM customers ORDER BY customer_id", mapper);
    }

    public Optional<Customer> findById(int id) {
        return jdbc.query("SELECT * FROM customers WHERE customer_id = ?", mapper, id).stream().findFirst();
    }

    public int create(Customer c) {
        return SqlHelper.insert(jdbc,
                "INSERT INTO customers (name, email, phone, address) VALUES (?, ?, ?, ?)",
                c.name(), c.email(), c.phone(), c.address());
    }

    public int update(int id, Customer c) {
        return jdbc.update(
                "UPDATE customers SET name = ?, email = ?, phone = ?, address = ? WHERE customer_id = ?",
                c.name(), c.email(), c.phone(), c.address(), id);
    }

    public int delete(int id) {
        return jdbc.update("DELETE FROM customers WHERE customer_id = ?", id);
    }
}
