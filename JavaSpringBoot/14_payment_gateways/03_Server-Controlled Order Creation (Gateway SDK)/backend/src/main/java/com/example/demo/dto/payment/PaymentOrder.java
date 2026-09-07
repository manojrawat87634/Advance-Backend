package com.example.demo.dto.payment;

/**
 * PaymentOrder
 */
// PaymentOrderRequest.java
public record PaymentOrderRequest(Long noteId) {}

// PaymentOrderResponse.java
public record PaymentOrderResponse(
    String gatewayOrderId,
    String orderReferenceId,
    Long amountInSubunits,
    String currency,
    String razorpayKeyId
) {}