package com.example.demo.services.payment;

import com.example.demo.dto.payment.PaymentOrderResponse;
import com.example.demo.models.notes.Note;
import com.example.demo.models.payments.PaymentOrder;
import com.example.demo.repo.notes.NoteRepository;
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
    private final NoteRepository noteRepository; // Inject Note Repository
    private final RazorpayClient razorpayClient;
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    public PaymentService(PaymentOrderRepository paymentOrderRepository, 
                          NoteRepository noteRepository, 
                          RazorpayClient razorpayClient) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.noteRepository = noteRepository;
        this.razorpayClient = razorpayClient;
    }

    @Transactional
    public PaymentOrderResponse createOrder(Long userId, Long noteId) throws RazorpayException {
        
        // 1. Fetch real note details from DB
        Note note = noteRepository.findById(noteId)
                .filter(n -> Boolean.TRUE.equals(n.getIsPublished()) && Boolean.FALSE.equals(n.getIsDeleted()))
                .orElseThrow(() -> new IllegalArgumentException("Note not found or unavailable"));

        // Extract real price and currency from the Note entity
        Long amountInSubunits = note.getPriceInSubunits(); // e.g. 29900L from DB
        String currency = note.getCurrency() != null ? note.getCurrency() : "INR";

        // 2. Check for existing active PENDING order for this EXACT amount
        LocalDateTime fifteenMinsAgo = LocalDateTime.now().minusMinutes(15);
        var existingOrder = paymentOrderRepository.findByUserIdAndStatus(userId, PaymentOrder.PaymentStatus.PENDING)
                .stream()
                .filter(order -> order.getAmountInSubunits().equals(amountInSubunits) 
                              && order.getCreatedAt().isAfter(fifteenMinsAgo))
                .findFirst();

        if (existingOrder.isPresent()) {
            PaymentOrder po = existingOrder.get();
            return new PaymentOrderResponse(
                po.getGatewayOrderId(), 
                po.getOrderReferenceId(), 
                po.getAmountInSubunits(), 
                po.getCurrency(), 
                razorpayKeyId
            );
        }

        // 3. Generate internal reference ID
        String orderReferenceId = "ORD-" + UUID.randomUUID().toString();

        // 4. Create Order on Razorpay Gateway using real note price
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInSubunits);
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", orderReferenceId);

        Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        String gatewayOrderId = razorpayOrder.get("id");

        // 5. Persist order record in MySQL
        PaymentOrder newOrder = new PaymentOrder();
        newOrder.setUserId(userId);
        newOrder.setOrderReferenceId(orderReferenceId);
        newOrder.setGatewayName("RAZORPAY");
        newOrder.setGatewayOrderId(gatewayOrderId);
        newOrder.setAmountInSubunits(amountInSubunits);
        newOrder.setCurrency(currency);
        newOrder.setStatus(PaymentOrder.PaymentStatus.PENDING);

        paymentOrderRepository.save(newOrder);

        return new PaymentOrderResponse(
            gatewayOrderId, 
            orderReferenceId, 
            amountInSubunits, 
            currency, 
            razorpayKeyId
        );
    }
}