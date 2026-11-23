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

import com.foodie.model.FoodOrder;
import com.foodie.model.OrderDetails;
import com.foodie.model.PlaceOrderRequest;
import com.foodie.model.StatusRequest;
import com.foodie.repository.OrderRepository;
import com.foodie.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;
    private final OrderRepository orders;

    public OrderController(OrderService service, OrderRepository orders) {
        this.service = service;
        this.orders = orders;
    }

    @GetMapping
    public List<FoodOrder> list() {
        return orders.findAll();
    }

    @GetMapping("/{id}")
    public OrderDetails get(@PathVariable int id) {
        return service.details(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDetails place(@Valid @RequestBody PlaceOrderRequest request) {
        return service.placeOrder(request);
    }

    @PutMapping("/{id}/status")
    public FoodOrder updateStatus(@PathVariable int id, @Valid @RequestBody StatusRequest request) {
        return service.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        service.deleteOrder(id);
    }
}
