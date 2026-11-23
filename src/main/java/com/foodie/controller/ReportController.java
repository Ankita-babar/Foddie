package com.foodie.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.foodie.repository.ReportRepository;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportRepository reports;

    public ReportController(ReportRepository reports) {
        this.reports = reports;
    }

    @GetMapping("/available-foods")
    public List<Map<String, Object>> availableFoods() {
        return reports.availableFoods();
    }

    @GetMapping("/orders-with-customers")
    public List<Map<String, Object>> ordersWithCustomers() {
        return reports.ordersWithCustomers();
    }

    @GetMapping("/orders-per-customer")
    public List<Map<String, Object>> ordersPerCustomer() {
        return reports.ordersPerCustomer();
    }

    @GetMapping("/top-selling-foods")
    public List<Map<String, Object>> topSellingFoods() {
        return reports.topSellingFoods();
    }

    @GetMapping("/restaurant-revenue")
    public List<Map<String, Object>> restaurantRevenue() {
        return reports.restaurantRevenue();
    }

    @GetMapping("/above-average-customers")
    public List<Map<String, Object>> aboveAverageCustomers() {
        return reports.aboveAverageCustomers();
    }

    @GetMapping("/monthly-stats")
    public List<Map<String, Object>> monthlyStats() {
        return reports.monthlyStats();
    }

    @GetMapping("/restaurants-with-many-dishes")
    public List<Map<String, Object>> restaurantsWithManyDishes() {
        return reports.restaurantsWithManyDishes();
    }
}
