package com.example._t1020159.controller;

import com.example._t1020159.dto.request.AlertsRequestDTO;
import com.example._t1020159.dto.response.AlertsResponseDTO;
import com.example._t1020159.service.AlertsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
public class AlertsController {

    @Autowired
    private AlertsService alertsService;

    // TẠO ALERT MỚI
    @PostMapping
    public ResponseEntity<AlertsResponseDTO> createAlert(@RequestBody AlertsRequestDTO requestDTO) {
        AlertsResponseDTO newAlert = alertsService.createAlert(requestDTO);
        return ResponseEntity.ok(newAlert);
    }

    // LẤY TẤT CẢ ALERTS
    @GetMapping
    public ResponseEntity<List<AlertsResponseDTO>> getAllAlerts() {
        List<AlertsResponseDTO> alerts = alertsService.getAllAlerts();
        return ResponseEntity.ok(alerts);
    }

    // 1. Lấy Alert theo ID
    @GetMapping("/{id}")
    public ResponseEntity<AlertsResponseDTO> getAlertById(@PathVariable Long id) {
        AlertsResponseDTO alert = alertsService.getAlertById(id);
        return ResponseEntity.ok(alert);
    }

    // 2. Cập nhật Alert theo ID
    @PutMapping("/{id}")
    public ResponseEntity<AlertsResponseDTO> updateAlert(@PathVariable Long id, @RequestBody AlertsRequestDTO requestDTO) {
        AlertsResponseDTO updatedAlert = alertsService.updateAlert(id, requestDTO);
        return ResponseEntity.ok(updatedAlert);
    }

    // 3. Xóa Alert
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        alertsService.deleteAlert(id);
        return ResponseEntity.noContent().build();
    }
}