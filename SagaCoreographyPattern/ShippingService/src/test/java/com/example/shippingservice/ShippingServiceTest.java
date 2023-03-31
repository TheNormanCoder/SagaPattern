package com.example.shippingservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ShippingServiceTest {

    @Autowired
    private ShipmentService shippingService;

    @MockBean
    private ShipmentRepository shipmentRepository;

    @Test
    void testFindByOrderId() {
        // Prepara i dati di input e i dati di ritorno previsti
        Long orderId = 1L;
        Shipment expectedShipment = new Shipment();
        expectedShipment.setOrderId(orderId);
        expectedShipment.setStatus(ShipmentStatus.PENDING);

        // Configura il comportamento del mock
        when(shipmentRepository.findByOrderId(orderId)).thenReturn(Optional.of(expectedShipment));

        // Esegui il metodo che vuoi testare
        Optional<Shipment> result = shippingService.findByOrderId(orderId);

        // Verifica i risultati con le asserzioni di JUnit
        assertTrue(result.isPresent(), "Shipment should be found");
        assertEquals(expectedShipment, result.get(), "Found shipment should match expected shipment");

        // Verifica che il metodo del mock sia stato chiamato
        verify(shipmentRepository, times(1)).findByOrderId(orderId);
    }
}
