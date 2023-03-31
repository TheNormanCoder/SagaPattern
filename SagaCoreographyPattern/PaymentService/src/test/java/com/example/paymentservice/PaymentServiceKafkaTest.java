package com.example.paymentservice;

;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentServiceKafkaTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private KafkaTemplate<String, PaymentStatusChangedEvent> kafkaTemplate;

    private Payment payment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        payment = new Payment();
        payment.setId(1L);
        payment.setOrderId(1L);
        payment.setStatus(PaymentStatus.PENDING);
    }

    @Test
    void testOnOrderCreated() {
        // Configura il comportamento del mock
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // Crea un evento OrderCreatedEvent
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(payment.getOrderId());

        // Chiama il metodo onOrderCreated
        paymentService.onOrderCreated(event);

        // Verifica che il metodo send del KafkaTemplate sia stato chiamato con gli argomenti corretti
        verify(kafkaTemplate, times(1)).send(eq("payment-status-changed"), any(PaymentStatusChangedEvent.class));
    }
}
