package com.foodie.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.foodie.model.FoodItem;

@Repository
public class FoodItemRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<FoodItem> mapper = (rs, n) -> new FoodItem(
            rs.getInt("food_id"),
            rs.getInt("restaurant_id"),
            rs.getString("food_name"),
            rs.getString("category"),
            rs.getBigDecimal("price"),
            rs.getBoolean("availability"));

    public FoodItemRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<FoodItem> findAll() {
        return jdbc.query("SELECT * FROM food_items ORDER BY food_id", mapper);
    }

    public List<FoodItem> findByRestaurant(int restaurantId) {
        return jdbc.query("SELECT * FROM food_items WHERE restaurant_id = ? ORDER BY food_id", mapper, restaurantId);
    }

    public Optional<FoodItem> findById(int id) {
        return jdbc.query("SELECT * FROM food_items WHERE food_id = ?", mapper, id).stream().findFirst();
    }

    public int create(FoodItem f) {
        return SqlHelper.insert(jdbc,
                "INSERT INTO food_items (restaurant_id, food_name, category, price, availability) VALUES (?, ?, ?, ?, ?)",
                f.restaurantId(), f.foodName(), f.category(), f.price(), available(f));
    }

    public int update(int id, FoodItem f) {
        return jdbc.update(
                "UPDATE food_items SET restaurant_id = ?, food_name = ?, category = ?, price = ?, availability = ? WHERE food_id = ?",
                f.restaurantId(), f.foodName(), f.category(), f.price(), available(f), id);
    }

    public int delete(int id) {
        return jdbc.update("DELETE FROM food_items WHERE food_id = ?", id);
    }

    private boolean available(FoodItem f) {
        if (f.availability() == null) {
            return true;
        }
        return f.availability();
    }
}
