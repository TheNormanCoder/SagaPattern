package com.example.paymentservice;

public class PaymentStatusChangedEvent {
    private Long orderId;
    private PaymentStatus status;

    public PaymentStatusChangedEvent(Long orderId, PaymentStatus status) {
        this.orderId = orderId;
        this.status = status;
    }

    // Getter e setter
}
