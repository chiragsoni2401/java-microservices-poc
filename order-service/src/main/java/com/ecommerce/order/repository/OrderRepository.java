package com.ecommerce.order.repository;

import com.ecommerce.order.entity.OrderEntity;
import com.ecommerce.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, String> {
    List<OrderEntity> findByCustomerId(String customerId);
    List<OrderEntity> findByStatus(OrderStatus status);
    Optional<OrderEntity> findByOrderId(String orderId);
    
    @Query("SELECT o FROM OrderEntity o WHERE o.customerId = :customerId ORDER BY o.createdAt DESC")
    List<OrderEntity> findRecentOrdersByCustomer(@Param("customerId") String customerId);
}
