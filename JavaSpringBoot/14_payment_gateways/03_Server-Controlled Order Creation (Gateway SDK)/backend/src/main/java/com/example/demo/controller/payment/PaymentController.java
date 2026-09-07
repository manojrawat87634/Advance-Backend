package com.example.demo.controller.payment;
// Adjust to your actual Auth Principal class
import com.example.demo.services.payment.PaymentService;
import com.razorpay.RazorpayException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.nio.file.attribute.UserPrincipal;
import java.util.Map;
import com.example.demo.dto.payment.PaymentOrderRequest;
import com.example.demo.dto.payment.PaymentOrderResponse;
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Endpoint triggered when the user clicks the "Buy Now" button on a note.
     * Generates a Razorpay Order ID and returns it to the frontend.
     */
    @PostMapping("/create-order")
    public ResponseEntity<?> createPaymentOrder(
            @RequestBody PaymentOrderRequest request,
            Authentication  authentication
    ) {
        // 1. Validate request payload
        if (request == null || request.noteId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "noteId is required"));
        }

        try {
            // 2. Extract authenticated user's ID
            // Long userId = currentUser.getId();
    Long userId = Long.parseLong(authentication.getPrincipal().toString());
            

            // 3. Delegate order creation logic to service layer
            PaymentOrderResponse response = paymentService.createOrder(userId, request.noteId());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Thrown if Note is not found, unpublished, or soft-deleted
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));

        } catch (RazorpayException e) {
            // Thrown if Razorpay API call fails
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to communicate with payment gateway: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred during order creation"));
        }
    }
}