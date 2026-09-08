package com.example.demo.dto.payment.verify;

/**
 * PaymentVerificationRequest
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentVerificationRequest(
    @JsonProperty("razorpay_order_id") String razorpay_order_id,
    @JsonProperty("razorpay_payment_id") String razorpay_payment_id,
    @JsonProperty("razorpay_signature") String razorpay_signature,
    Long noteId
) {}