package com.example.demo.dto;

import java.io.Serializable;

// Must implement Serializable so Spring AMQP can convert it
public class OrderEvent implements Serializable {
    private String orderId;
    private String item;
    private String userEmail;
    private double price;

    public OrderEvent() {}

    public OrderEvent(String orderId, String item, String userEmail, double price) {
        this.orderId = orderId;
        this.item = item;
        this.userEmail = userEmail;
        this.price = price;
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}