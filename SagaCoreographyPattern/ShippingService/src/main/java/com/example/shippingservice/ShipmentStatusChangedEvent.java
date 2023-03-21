package com.example.shippingservice;

public class ShipmentStatusChangedEvent {
    private Long orderId;
    private ShipmentStatus status;

    public ShipmentStatusChangedEvent(Long orderId, ShipmentStatus status) {
        this.orderId = orderId;
        this.status = status;
    }

    // Getter e setter
}
