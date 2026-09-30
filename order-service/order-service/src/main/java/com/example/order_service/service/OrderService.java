package com.example.order_service.service;

import com.example.order_service.dto.ProductResponse;
import com.example.order_service.entity.Order;
import com.example.order_service.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestClient restClient;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;

        this.restClient = RestClient
                .builder()
                .baseUrl("http://product-service:8081")
                .build();
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Order createOrder(Order order) {

        ProductResponse product = restClient
                .get()
                .uri("/products/{id}", order.getProductId())
                .retrieve()
                .body(ProductResponse.class);

        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        double totalPrice =
                product.getPrice() * order.getQuantity();

        order.setTotalPrice(totalPrice);

        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {

        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found");
        }

        orderRepository.deleteById(id);
    }
}