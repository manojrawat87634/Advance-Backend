package com.example.demo.controllers;

import com.example.demo.dto.OrderEvent; 
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${app.exchange.name:order.exchange}")
    private String exchange;

    @Value("${app.routing.key:order.created}")
    private String routingKey;

    @PostMapping
    public String placeOrder(@RequestParam String item, 
                             @RequestParam String email, 
                             @RequestParam double price) {
        
        long startTime = System.currentTimeMillis();

        // 1. Create Order Data
        String orderId = UUID.randomUUID().toString().substring(0, 8);
        OrderEvent event = new OrderEvent(orderId, item, email, price);

        // 2. Publish to RabbitMQ (Fire and Forget)
        rabbitTemplate.convertAndSend(exchange, routingKey, event);

        long endTime = System.currentTimeMillis();

        // 3. Instant Response
        return "Order placed! ID: " + orderId + " (Processed in " + (endTime - startTime) + " ms)";
    }
}