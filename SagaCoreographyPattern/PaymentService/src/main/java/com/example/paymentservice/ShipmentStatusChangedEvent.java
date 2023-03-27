package com.example.paymentservice;

public class ShipmentStatusChangedEvent {

    private Long orderId;
    private ShipmentStatus status;

    public ShipmentStatusChangedEvent() {
    }

    public ShipmentStatusChangedEvent(Long orderId, ShipmentStatus status) {
        this.orderId = orderId;
        this.status = status;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }
}
