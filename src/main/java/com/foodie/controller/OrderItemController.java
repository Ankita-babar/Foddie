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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.foodie.config.NotFoundException;
import com.foodie.model.OrderItem;
import com.foodie.model.OrderItemRequest;
import com.foodie.model.QuantityRequest;
import com.foodie.repository.OrderItemRepository;
import com.foodie.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    private final OrderService service;
    private final OrderItemRepository items;

    public OrderItemController(OrderService service, OrderItemRepository items) {
        this.service = service;
        this.items = items;
    }

    @GetMapping
    public List<OrderItem> list(@RequestParam(required = false) Integer orderId) {
        if (orderId == null) {
            return items.findAll();
        }
        return items.findByOrder(orderId);
    }

    @GetMapping("/{id}")
    public OrderItem get(@PathVariable int id) {
        return items.findById(id).orElseThrow(() -> new NotFoundException("Order item " + id + " not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderItem add(@Valid @RequestBody OrderItemRequest request) {
        return service.addItem(request.orderId(), request.foodId(), request.quantity());
    }

    @PutMapping("/{id}")
    public OrderItem updateQuantity(@PathVariable int id, @Valid @RequestBody QuantityRequest request) {
        return service.updateQuantity(id, request.quantity());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable int id) {
        service.removeItem(id);
    }
}
