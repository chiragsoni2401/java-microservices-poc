package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderDTO;
import com.ecommerce.order.entity.OrderEntity;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.event.OrderEvent;
import com.ecommerce.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private static final String ORDER_TOPIC = "order-events";
    private static final String CIRCUIT_BREAKER_NAME = "orderServiceCircuitBreaker";

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "createOrderFallback")
    public OrderDTO createOrder(OrderDTO orderDTO) {
        log.info("Creating order for customer: {}", orderDTO.getCustomerId());
        
        if (orderDTO.getQuantity() == null || orderDTO.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (orderDTO.getTotalPrice() == null || orderDTO.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total price must be greater than 0");
        }

        OrderEntity order = OrderEntity.builder()
                .customerId(orderDTO.getCustomerId())
                .productId(orderDTO.getProductId())
                .quantity(orderDTO.getQuantity())
                .totalPrice(orderDTO.getTotalPrice())
                .status(OrderStatus.PENDING)
                .build();

        OrderEntity savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {}", savedOrder.getOrderId());

        publishOrderEvent(savedOrder, "ORDER_CREATED");

        return convertToDTO(savedOrder);
    }

    public OrderDTO createOrderFallback(OrderDTO orderDTO, Exception ex) {
        log.error("Circuit breaker open for order creation. Cause: {}", ex.getMessage());
        throw new RuntimeException("Order service is temporarily unavailable. Please try again later.");
    }

    public OrderDTO getOrderById(String orderId) {
        log.info("Fetching order: {}", orderId);
        OrderEntity order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found: {}", orderId);
                    return new RuntimeException("Order not found: " + orderId);
                });
        return convertToDTO(order);
    }

    public List<OrderDTO> getOrdersByCustomerId(String customerId) {
        log.info("Fetching orders for customer: {}", customerId);
        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO updateOrderStatus(String orderId, OrderStatus newStatus) {
        log.info("Updating order {} status to {}", orderId, newStatus);
        
        OrderEntity order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found for status update: {}", orderId);
                    return new RuntimeException("Order not found: " + orderId);
                });
        
        order.setStatus(newStatus);
        OrderEntity updatedOrder = orderRepository.save(order);
        
        publishOrderEvent(updatedOrder, "ORDER_STATUS_UPDATED");
        
        log.info("Order {} status updated to {}", orderId, newStatus);
        return convertToDTO(updatedOrder);
    }

    private void publishOrderEvent(OrderEntity order, String eventType) {
        OrderEvent event = OrderEvent.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().toString())
                .eventType(eventType)
                .timestamp(System.currentTimeMillis())
                .build();

        kafkaTemplate.send(ORDER_TOPIC, order.getOrderId(), event);
        log.info("Order event published: {} for order {}", eventType, order.getOrderId());
    }

    private OrderDTO convertToDTO(OrderEntity order) {
        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().toString())
                .createdAt(order.getCreatedAt().toString())
                .updatedAt(order.getUpdatedAt().toString())
                .build();
    }
}
