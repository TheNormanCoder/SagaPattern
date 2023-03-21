package com.example.orderservice;

public class OrderCreatedEvent {
    private Long orderId;

    public OrderCreatedEvent(Long orderId) {
        this.orderId = orderId;
    }

    // Getter e setter
}
