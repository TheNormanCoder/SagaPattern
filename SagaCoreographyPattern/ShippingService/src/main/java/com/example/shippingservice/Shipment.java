package com.example.shippingservice;

import jakarta.persistence.*;


@Entity
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long orderId;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    // Altri campi come indirizzo di spedizione, data di consegna, ecc.

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    // Getter e setter per gli altri campi
}
