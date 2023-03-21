package com.example.shippingservice;

import java.io.Serializable;

public class PaymentStatusChangedEvent implements Serializable {
    private Long orderId;
    private PaymentStatus status;

    public PaymentStatusChangedEvent() {
    }

    public PaymentStatusChangedEvent(Long orderId, PaymentStatus status) {
        this.orderId = orderId;
        this.status = status;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}
