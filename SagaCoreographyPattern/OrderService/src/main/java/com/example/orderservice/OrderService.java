package com.example.orderservice;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // ... altri metodi esistenti ...

    @KafkaListener(topics = "shipment-status-changed", groupId = "order-service")
    public void onShipmentStatusChanged(ShipmentStatusChangedEvent event) {
        // Trova l'ordine con l'ID corrispondente
        Optional<Order> optionalOrder = orderRepository.findById(event.getOrderId());

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();

            // Aggiorna lo stato dell'ordine in base allo stato della spedizione
            switch (event.getStatus()) {
                case IN_TRANSIT:
                    order.setStatus(OrderStatus.IN_TRANSIT);
                    break;
                case COMPLETED:
                    order.setStatus(OrderStatus.COMPLETED);
                    break;
                case FAILED:
                    order.setStatus(OrderStatus.FAILED);
                    // Gestisci eventuali azioni correlate, come annullare l'ordine se la spedizione fallisce
                    // Ad esempio, potresti notificare l'utente o intraprendere altre azioni necessarie
                    break;
                default:
                    throw new IllegalArgumentException("Unknown shipment status: " + event.getStatus());
            }
            orderRepository.save(order);

        }
    }
}
