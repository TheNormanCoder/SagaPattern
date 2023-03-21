package com.example.shippingservice;

@Entity
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long orderId;

    @Enumerated(EnumType.STRING)
    private ShipmentStatus status;

    // Altri campi come indirizzo di spedizione, data di consegna, ecc.

    // Getter e setter
}
