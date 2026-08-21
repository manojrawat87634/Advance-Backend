package com.example.demo.dto.ecom.order;

public record CreateOrderResponse(
    Long dbOrderId,         // Your local `orders` table ID
    String razorpayOrderId, // The `order_xxx` ID returned by Razorpay API
    Long amount,            // Amount in PAISE
    String currency,        // Currency (e.g., "INR")
    String keyId            // Your Razorpay Key ID (needed by frontend SDK)
) {}