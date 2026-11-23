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
import com.foodie.model.Customer;
import com.foodie.model.FoodOrder;
import com.foodie.repository.CustomerRepository;
import com.foodie.repository.OrderRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customers;
    private final OrderRepository orders;

    public CustomerController(CustomerRepository customers, OrderRepository orders) {
        this.customers = customers;
        this.orders = orders;
    }

    @GetMapping
    public List<Customer> list() {
        return customers.findAll();
    }

    @GetMapping("/{id}")
    public Customer get(@PathVariable int id) {
        return find(id);
    }

    @GetMapping("/{id}/orders")
    public List<FoodOrder> ordersOf(@PathVariable int id) {
        find(id);
        return orders.findByCustomer(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Customer create(@Valid @RequestBody Customer customer) {
        return find(customers.create(customer));
    }

    @PutMapping("/{id}")
    public Customer update(@PathVariable int id, @Valid @RequestBody Customer customer) {
        if (customers.update(id, customer) == 0) {
            throw new NotFoundException("Customer " + id + " not found");
        }
        return find(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        if (customers.delete(id) == 0) {
            throw new NotFoundException("Customer " + id + " not found");
        }
    }

    private Customer find(int id) {
        return customers.findById(id).orElseThrow(() -> new NotFoundException("Customer " + id + " not found"));
    }
}
