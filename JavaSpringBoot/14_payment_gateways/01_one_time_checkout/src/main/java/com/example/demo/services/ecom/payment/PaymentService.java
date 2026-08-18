package com.example.demo.services.ecom.payment;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.dto.ecom.order.CreateOrderResponse;
import com.example.demo.dto.ecom.payment.PaymentVerificationRequest;
import com.example.demo.models.ecom.items.Item;
import com.example.demo.models.ecom.orders.OrderModel;
import com.example.demo.models.ecom.payments.PaymentModel;
import com.example.demo.repo.ecom.items.ItemRepository;
import com.example.demo.repo.ecom.orders.OrderRepository;
import com.example.demo.repo.ecom.payments.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Transactional
    public CreateOrderResponse createOrder(Long userId, Long itemId) throws Exception {
        // 1. Fetch item to get the price
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // 2. Prepare Razorpay order payload (amount is already in PAISE in your DB)
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", item.getPrice());
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "rcpt_user_" + userId + "_" + System.currentTimeMillis());

        // 3. Call Razorpay API
        Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        // 4. Save to `orders` table
        OrderModel newOrder = new OrderModel();
        newOrder.setUserId(userId);
        newOrder.setItemId(item.getId());
        newOrder.setAmount(item.getPrice());
        newOrder.setCurrency("INR");
        newOrder.setStatus(OrderModel.OrderStatus.CREATED);
        newOrder.setRazorpayOrderId(razorpayOrder.get("id"));
        OrderModel savedOrder = orderRepository.save(newOrder);

        return new CreateOrderResponse(
                savedOrder.getId(),
                savedOrder.getRazorpayOrderId(),
                savedOrder.getAmount(),
                savedOrder.getCurrency(),
                keyId
        );
    }

   @Transactional
    public boolean verifyAndSavePayment(Long userId, PaymentVerificationRequest req) {
        // 1. Verify Razorpay Signature using your helper method
        boolean isValid = verifySignature(req);

        if (!isValid) {
            return false; // Signature Mismatch or Invalid
        }

        // 2. Fetch corresponding DB order
        OrderModel order = orderRepository.findByRazorpayOrderId(req.razorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Order record missing"));

        // 3. Update `orders` status
        order.setStatus(OrderModel.OrderStatus.PAID);
        orderRepository.save(order);

        // 4. Insert record into `payments` table
        PaymentModel payment = new PaymentModel();
        payment.setOrderId(order.getId());
        payment.setUserId(userId);
        payment.setAmount(order.getAmount());
        payment.setCurrency(order.getCurrency());
        payment.setStatus(PaymentModel.PaymentStatus.SUCCESS);
        payment.setRazorpayPaymentId(req.razorpayPaymentId());
        payment.setRazorpaySignature(req.razorpaySignature());

        paymentRepository.save(payment);
        return true;
    }
    public boolean verifySignature(PaymentVerificationRequest req) {
    try {
        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", req.razorpayOrderId());
        options.put("razorpay_payment_id", req.razorpayPaymentId());
        options.put("razorpay_signature", req.razorpaySignature());

        // This built-in SDK method returns true if valid, or throws RazorpayException if invalid
        return Utils.verifyPaymentSignature(options, keySecret);
    } catch (RazorpayException e) {
        // Signature verification failed
        return false;
    }
}
}