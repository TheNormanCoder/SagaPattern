package com.example.paymentservice;

@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long orderId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    // Altri campi come importo, informazioni sulla carta di credito, ecc.

    // Getter e setter
}
