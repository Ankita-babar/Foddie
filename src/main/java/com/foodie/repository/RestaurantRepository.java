package com.foodie.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.foodie.model.Restaurant;

@Repository
public class RestaurantRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Restaurant> mapper = (rs, n) -> new Restaurant(
            rs.getInt("restaurant_id"),
            rs.getString("name"),
            rs.getString("address"),
            rs.getString("phone"),
            rs.getBigDecimal("rating"));

    public RestaurantRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Restaurant> findAll() {
        return jdbc.query("SELECT * FROM restaurants ORDER BY restaurant_id", mapper);
    }

    public Optional<Restaurant> findById(int id) {
        return jdbc.query("SELECT * FROM restaurants WHERE restaurant_id = ?", mapper, id).stream().findFirst();
    }

    public int create(Restaurant r) {
        return SqlHelper.insert(jdbc,
                "INSERT INTO restaurants (name, address, phone, rating) VALUES (?, ?, ?, ?)",
                r.name(), r.address(), r.phone(), rating(r));
    }

    public int update(int id, Restaurant r) {
        return jdbc.update(
                "UPDATE restaurants SET name = ?, address = ?, phone = ?, rating = ? WHERE restaurant_id = ?",
                r.name(), r.address(), r.phone(), rating(r), id);
    }

    public int delete(int id) {
        return jdbc.update("DELETE FROM restaurants WHERE restaurant_id = ?", id);
    }

    private BigDecimal rating(Restaurant r) {
        if (r.rating() == null) {
            return BigDecimal.ZERO;
        }
        return r.rating();
    }
}
