package com.example.paymentservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @MockBean
    private PaymentRepository paymentRepository;

    @Test
    void testFindByOrderId() {
        // Prepara i dati di input e i dati di ritorno previsti
        Long orderId = 1L;
        Payment expectedPayment = new Payment();
        expectedPayment.setOrderId(orderId);
        expectedPayment.setStatus(PaymentStatus.PENDING);

        // Configura il comportamento del mock
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(expectedPayment));

        // Esegui il metodo che vuoi testare
        Optional<Payment> result = paymentService.findByOrderId(orderId);

        // Verifica i risultati con le asserzioni di JUnit
        assertTrue(result.isPresent(), "Payment should be found");
        assertEquals(expectedPayment, result.get(), "Found payment should match expected payment");

        // Verifica che il metodo del mock sia stato chiamato
        verify(paymentRepository, times(1)).findByOrderId(orderId);
    }
}
