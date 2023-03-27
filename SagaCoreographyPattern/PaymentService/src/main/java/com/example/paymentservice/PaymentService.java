package com.example.paymentservice;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentStatusChangedEvent> kafkaTemplate;

    public PaymentService(PaymentRepository paymentRepository, KafkaTemplate<String, PaymentStatusChangedEvent> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Optional<Payment> findByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @KafkaListener(topics = "order-created", groupId = "payment-service")
    public void onOrderCreated(OrderCreatedEvent event) {
        Payment payment = new Payment();
        payment.setOrderId(event.getOrderId());
        payment.setStatus(PaymentStatus.PENDING);

        // Esegui la logica di pagamento e aggiorna lo stato del pagamento
        // (SUCCEEDED o FAILED)

        Payment savedPayment = paymentRepository.save(payment);

        PaymentStatusChangedEvent statusChangedEvent = new PaymentStatusChangedEvent(savedPayment.getOrderId(), savedPayment.getStatus());
        kafkaTemplate.send("payment-status-changed", statusChangedEvent);
    }

    @KafkaListener(topics = "shipment-status-changed", groupId = "payment-service")
    public void onShipmentStatusChanged(ShipmentStatusChangedEvent event) {
        // Trova il pagamento corrispondente all'ID dell'ordine ricevuto nell'evento
        Optional<Payment> optionalPayment = findByOrderId(event.getOrderId());

        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();

            // Aggiorna lo stato del pagamento in base allo stato della spedizione
            if (event.getStatus() == ShipmentStatus.COMPLETED) {
                payment.setStatus(PaymentStatus.CONFIRMED);
            } else if (event.getStatus() == ShipmentStatus.FAILED) {
                payment.setStatus(PaymentStatus.CANCELLED);
            }

            // Salva il pagamento aggiornato nel repository
            paymentRepository.save(payment);

            // Puoi anche gestire eventuali azioni correlate, come notificare l'utente
            // Ad esempio, inviare un'email di notifica o aggiornare l'interfaccia utente
        }
    }

}
