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
}
