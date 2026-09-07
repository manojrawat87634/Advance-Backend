package com.example.demo.services.payment;
import com.example.demo.models.payments.PaymentOrder;
import com.example.demo.repo.payment.PaymentOrderRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    public PaymentService(PaymentOrderRepository paymentOrderRepository, RazorpayClient razorpayClient) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.razorpayClient = razorpayClient;
    }

    @Transactional
    public PaymentOrderResponse createOrder(Long userId, Long noteId) throws RazorpayException {
        // 1. Fetch note details (Replace with your NoteRepository query)
        // Note note = noteRepository.findByIdAndIsPublishedTrue(noteId).orElseThrow();
        Long amountInSubunits = 29900L; // Example: ₹299.00
        String currency = "INR";

        // 2. Optional Optimization: Reuse existing PENDING order created within the last 15 minutes
        LocalDateTime fifteenMinsAgo = LocalDateTime.now().minusMinutes(15);
        var existingOrder = paymentOrderRepository.findByUserIdAndStatus(userId, PaymentOrder.PaymentStatus.PENDING)
                .stream()
                .filter(order -> order.getAmountInSubunits().equals(amountInSubunits) && order.getCreatedAt().isAfter(fifteenMinsAgo))
                .findFirst();

        if (existingOrder.isPresent()) {
            PaymentOrder po = existingOrder.get();
            return new PaymentOrderResponse(po.getGatewayOrderId(), po.getOrderReferenceId(), po.getAmountInSubunits(), po.getCurrency(), razorpayKeyId);
        }

        // 3. Generate unique order reference ID
        String orderReferenceId = "ORD-" + UUID.randomUUID().toString();

        // 4. Create Order on Razorpay Gateway
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInSubunits);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", orderReferenceId);

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        String gatewayOrderId = razorpayOrder.get("id");

        // 5. Persist order in MySQL using PaymentOrderRepository
        PaymentOrder newOrder = new PaymentOrder();
        newOrder.setUserId(userId);
        newOrder.setOrderReferenceId(orderReferenceId);
        newOrder.setGatewayName("RAZORPAY");
        newOrder.setGatewayOrderId(gatewayOrderId);
        newOrder.setAmountInSubunits(amountInSubunits);
        newOrder.setCurrency(currency);
        newOrder.setStatus(PaymentOrder.PaymentStatus.PENDING);

        paymentOrderRepository.save(newOrder);
        // 6. Return response to Controller
        return new PaymentOrderResponse(gatewayOrderId, orderReferenceId, amountInSubunits, currency, razorpayKeyId);
    }
}