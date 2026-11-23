package com.foodie.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.foodie.config.NotFoundException;
import com.foodie.model.FoodItem;
import com.foodie.model.Restaurant;
import com.foodie.repository.FoodItemRepository;
import com.foodie.repository.RestaurantRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantRepository restaurants;
    private final FoodItemRepository foods;

    public RestaurantController(RestaurantRepository restaurants, FoodItemRepository foods) {
        this.restaurants = restaurants;
        this.foods = foods;
    }

    @GetMapping
    public List<Restaurant> list() {
        return restaurants.findAll();
    }

    @GetMapping("/{id}")
    public Restaurant get(@PathVariable int id) {
        return find(id);
    }

    @GetMapping("/{id}/foods")
    public List<FoodItem> menu(@PathVariable int id) {
        find(id);
        return foods.findByRestaurant(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Restaurant create(@Valid @RequestBody Restaurant restaurant) {
        return find(restaurants.create(restaurant));
    }

    @PutMapping("/{id}")
    public Restaurant update(@PathVariable int id, @Valid @RequestBody Restaurant restaurant) {
        if (restaurants.update(id, restaurant) == 0) {
            throw new NotFoundException("Restaurant " + id + " not found");
        }
        return find(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        if (restaurants.delete(id) == 0) {
            throw new NotFoundException("Restaurant " + id + " not found");
        }
    }

    private Restaurant find(int id) {
        return restaurants.findById(id).orElseThrow(() -> new NotFoundException("Restaurant " + id + " not found"));
    }
}
