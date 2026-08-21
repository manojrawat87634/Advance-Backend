package com.example.demo.repo.ecom.orders;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.models.ecom.orders.OrderModel;
import com.example.demo.models.ecom.orders.OrderModel.OrderStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderModel, Long> {

    // 1. Find order by Razorpay Order ID (Crucial for Payment Verification & Webhooks)
    Optional<OrderModel> findByRazorpayOrderId(String razorpayOrderId);

    // 2. Find all orders created by a specific user
    List<OrderModel> findByUserId(Long userId);

    // 3. Paginated user orders (ideal for order history pages)
    Page<OrderModel> findByUserId(Long userId, Pageable pageable);

    // 4. Find user orders filtered by status (Uses index idx_orders_user_status)
    List<OrderModel> findByUserIdAndStatus(Long userId, OrderStatus status);

    // 5. Check if a user has already purchased a specific item successfully
    boolean existsByUserIdAndItemIdAndStatus(Long userId, Long itemId, OrderStatus status);
}