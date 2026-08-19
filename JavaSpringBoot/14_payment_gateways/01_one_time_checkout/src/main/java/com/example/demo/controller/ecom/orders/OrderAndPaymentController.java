package com.example.demo.controller.ecom.orders;


import com.example.demo.dto.ecom.order.CreateOrderRequest;
import com.example.demo.dto.ecom.order.CreateOrderResponse;
import com.example.demo.dto.ecom.payment.PaymentVerificationRequest;
import com.example.demo.services.ecom.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderAndPaymentController {

    private final PaymentService paymentService;

    /**
     * 1. CREATE ORDER
     * Endpoint: POST /api/orders/create
     * Triggered when user clicks "Buy / Checkout" in React.
     */
    @PostMapping("/orders/create")
    public ResponseEntity<?> createOrder(
            @RequestBody CreateOrderRequest request,
            @RequestHeader("X-User-Id") Long userId) { // Or extract userId from JWT SecurityContext
        
        try {
            CreateOrderResponse response = paymentService.createOrder(userId, request.itemId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create order: " + e.getMessage()));
        }
    }

    /**
     * 2. VERIFY PAYMENT
     * Endpoint: POST /api/payments/verify
     * Triggered by React handler callback immediately after Checkout modal succeeds.
     */
    @PostMapping("/payments/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody PaymentVerificationRequest request,
            @RequestHeader("X-User-Id") Long userId) { // Or extract userId from JWT SecurityContext

        boolean isVerified = paymentService.verifyAndSavePayment(userId, request);

        if (isVerified) {
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Payment verified successfully and order updated"
            ));
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "status", "FAILED",
                            "message", "Invalid payment signature verification failed"
                    ));
        }
    }
}
