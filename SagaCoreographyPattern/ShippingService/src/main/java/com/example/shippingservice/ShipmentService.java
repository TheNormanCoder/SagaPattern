package com.example.shippingservice;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final KafkaTemplate<String, ShipmentStatusChangedEvent> kafkaTemplate;

    public ShipmentService(ShipmentRepository shipmentRepository, KafkaTemplate<String, ShipmentStatusChangedEvent> kafkaTemplate) {
        this.shipmentRepository = shipmentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Optional<Shipment> findByOrderId(Long orderId) {
        return shipmentRepository.findByOrderId(orderId);
    }

    @KafkaListener(topics = "payment-status-changed", groupId = "shipping-service")
    public void onPaymentStatusChanged(PaymentStatusChangedEvent event) {
        if (event.getStatus() == PaymentStatus.SUCCEEDED) {
            Shipment shipment = new Shipment();
            shipment.setOrderId(event.getOrderId());
            shipment.setStatus(ShipmentStatus.PENDING);

            // Esegui la logica di spedizione e aggiorna lo stato della spedizione
            // (SHIPPED o FAILED)

            Shipment savedShipment = shipmentRepository.save(shipment);

            ShipmentStatusChangedEvent statusChangedEvent = new ShipmentStatusChangedEvent(savedShipment.getOrderId(), savedShipment.getStatus());
            kafkaTemplate.send("shipment-status-changed", statusChangedEvent);
        }
    }
}
