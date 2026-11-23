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

import com.foodie.config.BadRequestException;
import com.foodie.config.NotFoundException;
import com.foodie.model.FoodOrder;
import com.foodie.model.Payment;
import com.foodie.model.PaymentRequest;
import com.foodie.model.StatusRequest;
import com.foodie.repository.OrderRepository;
import com.foodie.repository.PaymentRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final List<String> METHODS = List.of("CARD", "UPI", "COD");
    private static final List<String> STATUSES = List.of("PENDING", "PAID", "FAILED", "REFUNDED");

    private final PaymentRepository payments;
    private final OrderRepository orders;

    public PaymentController(PaymentRepository payments, OrderRepository orders) {
        this.payments = payments;
        this.orders = orders;
    }

    @GetMapping
    public List<Payment> list() {
        return payments.findAll();
    }

    @GetMapping("/{id}")
    public Payment get(@PathVariable int id) {
        return find(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Payment record(@Valid @RequestBody PaymentRequest request) {
        validate(request.paymentMethod(), METHODS, "Payment method");
        validate(request.paymentStatus(), STATUSES, "Payment status");
        FoodOrder order = orders.findById(request.orderId())
                .orElseThrow(() -> new NotFoundException("Order " + request.orderId() + " not found"));
        int id = payments.create(order.orderId(), request.paymentMethod(), request.paymentStatus(), order.totalAmount());
        return find(id);
    }

    @PutMapping("/{id}")
    public Payment updateStatus(@PathVariable int id, @Valid @RequestBody StatusRequest request) {
        validate(request.status(), STATUSES, "Payment status");
        if (payments.updateStatus(id, request.status()) == 0) {
            throw new NotFoundException("Payment " + id + " not found");
        }
        return find(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        if (payments.delete(id) == 0) {
            throw new NotFoundException("Payment " + id + " not found");
        }
    }

    private void validate(String value, List<String> allowed, String label) {
        if (!allowed.contains(value)) {
            throw new BadRequestException(label + " must be one of " + allowed);
        }
    }

    private Payment find(int id) {
        return payments.findById(id).orElseThrow(() -> new NotFoundException("Payment " + id + " not found"));
    }
}
