package com.example.demo.services.ecom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.demo.models.auth.UserModel;
import com.example.demo.models.ecom.OrderModel;
import com.example.demo.models.ecom.ItemModel;
import com.example.demo.models.ecom.enums.*;;
import jakarta.transaction.Transactional;

/**
 * PaymentService
 */
@Service
@Transactional
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    // STEP 1 & 2: SECURE ORDER CREATION
    public OrderResponseDto createOrder(Long itemId, UserModel currentUser) throws Exception {
        // Rule 1: Fetch price FROM DATABASE, never from request payload
        ItemModel item = itemRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item not found or inactive"));

        RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", item.getPrice()); // Amount in paise straight from DB
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "order_rcptid_" + System.currentTimeMillis());

        com.razorpay.Order rzpOrder = razorpayClient.orders.create(orderRequest);

        // Save order in DB
        OrderModel order = OrderModel.builder()
            .user(currentUser)
            .item(item)
            .amount(item.getPrice())
            .currency("INR")
            .status(OrderStatus.CREATED)
            .razorpayOrderId(rzpOrder.get("id"))
            .build();

        orderRepository.save(order);

        return new OrderResponseDto(order.getRazorpayOrderId(), order.getAmount(), order.getCurrency(), keyId);
    }

    // STEP 4: SECURE PAYMENT VERIFICATION
    public boolean verifyAndSavePayment(PaymentVerificationDto dto, UserModel currentUser) throws Exception {
        // Rule 2: Recompute HMAC-SHA256 signature using Secret Key
        String payload = dto.getRazorpayOrderId() + "|" + dto.getRazorpayPaymentId();
        boolean isValid = Utils.verifySignature(payload, dto.getRazorpaySignature(), keySecret);

        if (!isValid) {
            throw new SecurityException("Tampered payment payload detected!");
        }

        OrderModel order = orderRepository.findByRazorpayOrderId(dto.getRazorpayOrderId())
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Update Order Status
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        // Record Payment
        PaymentModel payment = PaymentModel.builder()
            .order(order)
            .user(currentUser)
            .amount(order.getAmount())
            .currency(order.getCurrency())
            .status(PaymentStatus.SUCCESS)
            .razorpayPaymentId(dto.getRazorpayPaymentId())
            .razorpaySignature(dto.getRazorpaySignature())
            .build();
        paymentRepository.save(payment);
        return true;
    }
}