package com.example.demo.services.payment;
import com.razorpay.Utils;
import com.example.demo.dto.payment.PaymentOrderResponse;
import com.example.demo.models.ecom.enums.PaymentStatus;
import com.example.demo.models.notes.Note;
import com.example.demo.models.payments.PaymentOrder;
import com.example.demo.models.payments.PaymentTransaction;
import com.example.demo.models.payments.UserEntitlement;
import com.example.demo.repo.notes.NoteRepository;
import com.example.demo.repo.payment.PaymentOrderRepository;
import com.example.demo.repo.payment.PaymentTransactionRepository;
import com.example.demo.repo.payment.UserEntitlementRepository;
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
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final UserEntitlementRepository userEntitlementRepository;
    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;
    public PaymentService(PaymentOrderRepository paymentOrderRepository, 
                          NoteRepository noteRepository, 
                          RazorpayClient razorpayClient,
                        PaymentTransactionRepository paymentTransactionRepository,
                    
                    UserEntitlementRepository userEntitlementRepository) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.noteRepository = noteRepository;
        this.razorpayClient = razorpayClient;
        this.userEntitlementRepository = userEntitlementRepository;
        
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


@Transactional
    public boolean verifyPayment(
            Long userId,
            Long noteId,
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature
    ) {
        // 1. Verify Razorpay HMAC SHA256 Signature
        boolean isValidSignature = verifyRazorpaySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature);
        if (!isValidSignature) {
            return false;
        }

        // 2. Retrieve Payment Order from DB
        PaymentOrder order = paymentOrderRepository.findByGatewayOrderId(razorpayOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found for gateway ID: " + razorpayOrderId));

        // Ensure current user matches the order owner
        if (!order.getUserId().equals(userId)) {
            throw new SecurityException("User mismatch for this order.");
        }

        // 3. Update Payment Order Status (Fixes Error #1: Inner enum from PaymentOrder)
        order.setStatus(PaymentOrder.PaymentStatus.PAID);
        paymentOrderRepository.save(order);
        // 4. Record Payment Transaction entry in payment_transactions table
        PaymentTransaction transaction = new PaymentTransaction();
        // Fixes Error #2: If field is PaymentOrder entity reference use setPaymentOrder,
        // or if it's a raw ID field, match the field name in PaymentTransaction.java
        transaction.setPaymentOrder(order); 
        transaction.setGatewayPaymentId(razorpayPaymentId);
        transaction.setGatewaySignature(razorpaySignature);
        transaction.setAmountInSubunits(order.getAmountInSubunits());
        
        // Fixes Error #3: Use the TransactionStatus Enum instead of String "SUCCESS"
        transaction.setStatus(PaymentTransaction.TransactionStatus.SUCCESS);
        transaction.setCreatedAt(LocalDateTime.now());
        
        PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);

        // 5. Fetch Note to retrieve its associated media_asset_id
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found: " + noteId));

        // 6. Grant Entitlement in user_entitlements table
        boolean alreadyEntitled = userEntitlementRepository
                .existsByUserIdAndMediaAssetId(userId, note.getMediaAssetId());

        if (!alreadyEntitled) {
            UserEntitlement entitlement = new UserEntitlement();
            entitlement.setUserId(userId);
            entitlement.setMediaAssetId(note.getMediaAssetId()); // Direct link to media_assets
            // entitlement.setGrantedByTransactionId(savedTransaction.getId()); // FK link to transaction
            entitlement.setGrantedByTransaction(savedTransaction);
            entitlement.setIsActive(true);
            entitlement.setExpiresAt(null); // Lifetime access
            entitlement.setCreatedAt(LocalDateTime.now());
            entitlement.setUpdatedAt(LocalDateTime.now());
            userEntitlementRepository.save(entitlement);
        }
        return true;
    }

    private boolean verifyRazorpaySignature(String orderId, String paymentId, String signature) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);
            return Utils.verifyPaymentSignature(options, razorpaySecret);
        } catch (Exception e) {
            System.err.println("Signature verification failed: " + e.getMessage());
            return false;
        }
    }

}