package com.example.paymentservice;

import java.io.Serializable;

public class OrderCreatedEvent implements Serializable {
    private Long orderId;
    // Altri campi dell'evento, come ad esempio dettagli dell'ordine, importo, ecc.

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(Long orderId /*, altri parametri per i campi*/) {
        this.orderId = orderId;
        // Inizializza gli altri campi
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    // Getter e setter per gli altri campi
}
