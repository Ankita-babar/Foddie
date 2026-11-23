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
import com.foodie.repository.FoodItemRepository;
import com.foodie.service.FoodMediaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/foods")
public class FoodItemController {

    private final FoodItemRepository foods;
    private final FoodMediaService media;

    public FoodItemController(FoodItemRepository foods, FoodMediaService media) {
        this.foods = foods;
        this.media = media;
    }

    @GetMapping
    public List<FoodItem> list() {
        return foods.findAll();
    }

    @GetMapping("/{id}")
    public FoodItem get(@PathVariable int id) {
        return find(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FoodItem create(@Valid @RequestBody FoodItem food) {
        return find(foods.create(food));
    }

    @PutMapping("/{id}")
    public FoodItem update(@PathVariable int id, @Valid @RequestBody FoodItem food) {
        if (foods.update(id, food) == 0) {
            throw new NotFoundException("Food item " + id + " not found");
        }
        return find(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        if (foods.delete(id) == 0) {
            throw new NotFoundException("Food item " + id + " not found");
        }
        media.removeAll(id);
    }

    private FoodItem find(int id) {
        return foods.findById(id).orElseThrow(() -> new NotFoundException("Food item " + id + " not found"));
    }
}
