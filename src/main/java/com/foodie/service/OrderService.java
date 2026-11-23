package com.foodie.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foodie.config.BadRequestException;
import com.foodie.config.NotFoundException;
import com.foodie.model.FoodItem;
import com.foodie.model.FoodOrder;
import com.foodie.model.ItemLine;
import com.foodie.model.OrderDetails;
import com.foodie.model.OrderItem;
import com.foodie.model.Payment;
import com.foodie.model.PlaceOrderRequest;
import com.foodie.repository.CustomerRepository;
import com.foodie.repository.FoodItemRepository;
import com.foodie.repository.OrderItemRepository;
import com.foodie.repository.OrderRepository;
import com.foodie.repository.PaymentRepository;

@Service
public class OrderService {

    private static final List<String> STATUSES =
            List.of("PLACED", "PREPARING", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED");
    private static final List<String> EDITABLE = List.of("PLACED", "PREPARING");
    private static final List<String> METHODS = List.of("CARD", "UPI", "COD");

    private final CustomerRepository customers;
    private final FoodItemRepository foods;
    private final OrderRepository orders;
    private final OrderItemRepository items;
    private final PaymentRepository payments;

    public OrderService(CustomerRepository customers, FoodItemRepository foods, OrderRepository orders,
            OrderItemRepository items, PaymentRepository payments) {
        this.customers = customers;
        this.foods = foods;
        this.orders = orders;
        this.items = items;
        this.payments = payments;
    }

    @Transactional
    public OrderDetails placeOrder(PlaceOrderRequest request) {
        if (!METHODS.contains(request.paymentMethod())) {
            throw new BadRequestException("Payment method must be CARD, UPI or COD");
        }
        customers.findById(request.customerId())
                .orElseThrow(() -> new NotFoundException("Customer " + request.customerId() + " not found"));

        int orderId = orders.create(request.customerId());
        for (ItemLine line : request.items()) {
            addLine(orderId, line.foodId(), line.quantity());
        }
        orders.recalculateTotal(orderId);

        FoodOrder order = orders.findById(orderId).orElseThrow();
        String paymentStatus = "PAID";
        if (request.paymentMethod().equals("COD")) {
            paymentStatus = "PENDING";
        }
        payments.create(orderId, request.paymentMethod(), paymentStatus, order.totalAmount());
        return details(orderId);
    }

    @Transactional(readOnly = true)
    public OrderDetails details(int orderId) {
        FoodOrder order = findOrder(orderId);
        return new OrderDetails(order, items.findByOrder(orderId), payments.findByOrder(orderId).orElse(null));
    }

    @Transactional
    public OrderItem addItem(int orderId, int foodId, int quantity) {
        requireEditable(orderId);
        addLine(orderId, foodId, quantity);
        refreshTotals(orderId);
        return items.findByOrder(orderId).stream()
                .filter(i -> i.foodId() == foodId)
                .findFirst()
                .orElseThrow();
    }

    @Transactional
    public OrderItem updateQuantity(int itemId, int quantity) {
        OrderItem item = findItem(itemId);
        requireEditable(item.orderId());
        items.updateQuantity(itemId, quantity);
        refreshTotals(item.orderId());
        return findItem(itemId);
    }

    @Transactional
    public void removeItem(int itemId) {
        OrderItem item = findItem(itemId);
        requireEditable(item.orderId());
        items.delete(itemId);
        refreshTotals(item.orderId());
    }

    @Transactional
    public FoodOrder updateStatus(int orderId, String status) {
        if (!STATUSES.contains(status)) {
            throw new BadRequestException("Status must be one of " + STATUSES);
        }
        findOrder(orderId);
        orders.updateStatus(orderId, status);

        Payment payment = payments.findByOrder(orderId).orElse(null);
        if (payment != null) {
            if (status.equals("CANCELLED") && payment.paymentStatus().equals("PAID")) {
                payments.updateStatus(payment.paymentId(), "REFUNDED");
            } else if (status.equals("CANCELLED") && payment.paymentStatus().equals("PENDING")) {
                payments.updateStatus(payment.paymentId(), "FAILED");
            } else if (status.equals("DELIVERED") && payment.paymentStatus().equals("PENDING")) {
                payments.updateStatus(payment.paymentId(), "PAID");
            }
        }
        return findOrder(orderId);
    }

    @Transactional
    public void deleteOrder(int orderId) {
        if (orders.delete(orderId) == 0) {
            throw new NotFoundException("Order " + orderId + " not found");
        }
    }

    private void addLine(int orderId, int foodId, int quantity) {
        FoodItem food = foods.findById(foodId)
                .orElseThrow(() -> new NotFoundException("Food item " + foodId + " not found"));
        if (!food.availability()) {
            throw new BadRequestException(food.foodName() + " is currently not available");
        }
        items.addOrIncrease(orderId, foodId, quantity, food.price());
    }

    private void refreshTotals(int orderId) {
        orders.recalculateTotal(orderId);
        payments.syncAmount(orderId);
    }

    private void requireEditable(int orderId) {
        FoodOrder order = findOrder(orderId);
        if (!EDITABLE.contains(order.status())) {
            throw new BadRequestException("Items can only be changed while the order is PLACED or PREPARING");
        }
    }

    private FoodOrder findOrder(int orderId) {
        return orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order " + orderId + " not found"));
    }

    private OrderItem findItem(int itemId) {
        return items.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Order item " + itemId + " not found"));
    }
}
