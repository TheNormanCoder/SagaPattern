package com.example.orderservice;

@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // Altri campi come importo, indirizzo di spedizione, ecc.

    // Getter e setter
}
