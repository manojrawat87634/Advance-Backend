// PaymentOrderResponse.java

package com.example.demo.dto.payment;

public record PaymentOrderResponse(
    String gatewayOrderId,
    String orderReferenceId,
    Long amountInSubunits,
    String currency,
    String razorpayKeyId
) {}