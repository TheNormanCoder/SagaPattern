package com.example.shippingservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/shipments")
public class ShipmentController {
    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Shipment> getShipmentByOrderId(@PathVariable Long orderId) {
        Optional<Shipment> shipment = shipmentService.findByOrderId(orderId);
        return shipment.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
