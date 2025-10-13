package com.example._t1020159.service;

import com.example._t1020159.dto.request.AlertsRequestDTO;
import com.example._t1020159.dto.response.AlertsResponseDTO;
import com.example._t1020159.entity.AlertsEntity;
import com.example._t1020159.entity.ItemEntity;
import com.example._t1020159.repository.AlertsRepository;
import com.example._t1020159.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertsService {

    @Autowired
    private AlertsRepository alertsRepository;

    @Autowired
    private ItemRepository itemRepository;

    private AlertsResponseDTO mapToResponseDTO(AlertsEntity entity) {
        return AlertsResponseDTO.builder()
                .id(entity.getId())
                .message(entity.getMessage())
                .alertBefore(entity.getAlertBefore())
                .isSent(entity.isSent())
                .itemId(entity.getItem().getId())
                .build();
    }

    // 1. Tạo Alert mới
    public AlertsResponseDTO createAlert(AlertsRequestDTO requestDTO) {

        if (requestDTO.getItemId() == null) {
            throw new IllegalArgumentException("Item ID is required for creating an alert.");
        }

        ItemEntity item = itemRepository.findById(requestDTO.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + requestDTO.getItemId()));

        AlertsEntity newAlert = AlertsEntity.builder()
                .message(requestDTO.getMessage())
                .alertBefore(requestDTO.getAlertBefore())
                .isSent(false)
                .item(item)
                .build();

        AlertsEntity savedAlert = alertsRepository.save(newAlert);
        return mapToResponseDTO(savedAlert);
    }

    // 2. Lấy Alert theo ID (READ BY ID)
    public AlertsResponseDTO getAlertById(Long id){
        AlertsEntity alert = alertsRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Alert not found with id: " + id));
        return mapToResponseDTO(alert);
    }

    // 3. Cập nhật Alert theo ID (UPDATE)
    public AlertsResponseDTO updateAlert(Long id, AlertsRequestDTO requestDTO){
        AlertsEntity existingAlert = alertsRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Alert not found with id: " + id));

        existingAlert.setMessage(requestDTO.getMessage());
        existingAlert.setAlertBefore(requestDTO.getAlertBefore());

        AlertsEntity updatedAlert = alertsRepository.save(existingAlert);
        return mapToResponseDTO(updatedAlert);
    }

    // 4. Lấy tất cả Alerts (READ ALL)
    public List<AlertsResponseDTO> getAllAlerts() {
        return alertsRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // 5. Xóa Alert (DELETE)
    public void deleteAlert(Long id) {
        if (!alertsRepository.existsById(id)) {
            throw new EntityNotFoundException("Alert not found with id: " + id);
        }
        alertsRepository.deleteById(id);
    }
}