package com.example._t1020159.service;

import com.example._t1020159.dto.request.ItemRequestDTO;
import com.example._t1020159.dto.response.ItemResponseDTO;
import com.example._t1020159.entity.CategoriesEntity;
import com.example._t1020159.entity.ItemEntity;
import com.example._t1020159.repository.CategoriesRepository;
import com.example._t1020159.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DetaiService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    /**
     * Tạo mới detai-task (lặp lại DAILY/WEEK)
     */
    @Transactional
    public List<ItemResponseDTO> createDetaiTask(ItemRequestDTO requestDTO) {

        if (requestDTO.getCategoryId() == null) {
            throw new IllegalArgumentException("Category ID must be provided.");
        }

        CategoriesEntity category = categoriesRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        int repeatCount;
        String interval = requestDTO.getRecurrenceInterval().toUpperCase();

        repeatCount = switch (interval) {
            case "DAILY" -> 30;
            case "WEEK", "WEEKLY" -> 7;
            default -> 1;
        };

        LocalDateTime currentStart = requestDTO.getStart();
        LocalDateTime currentDue = requestDTO.getDue();

        for (int i = 0; i < repeatCount; i++) {
            ItemEntity newItem = ItemEntity.builder()
                    .prioritize(requestDTO.getPrioritize())
                    .title(requestDTO.getTitle() + " (Lần " + (i + 1) + ")")
                    .description(requestDTO.getDescription())
                    .start(currentStart)
                    .due(currentDue)
                    .status(false)
                    .isRecurring(true)
                    .recurrenceInterval(interval)
                    .category(category)
                    .build();

            itemRepository.save(newItem);

            if (interval.equals("DAILY")) {
                currentStart = currentStart.plusDays(1);
                currentDue = currentDue.plusDays(1);
            } else if (interval.equals("WEEK") || interval.equals("WEEKLY")) {
                currentStart = currentStart.plusWeeks(1);
                currentDue = currentDue.plusWeeks(1);
            }
        }

        List<ItemEntity> recurringTasks = itemRepository.findByIsRecurringAndRecurrenceInterval(true, interval);

        return recurringTasks.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy toàn bộ detai-task (các task lặp lại)
     */
    public List<ItemResponseDTO> getAllDetaiTasks() {
        List<ItemEntity> recurringTasks = itemRepository.findByIsRecurring(true);
        return recurringTasks.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Hàm tiện ích chuyển Entity sang DTO
     */
    private ItemResponseDTO mapToResponseDTO(ItemEntity entity) {
        Long categoryId = (entity.getCategory() != null) ? entity.getCategory().getId() : null;

        return ItemResponseDTO.builder()
                .id(entity.getId())
                .prioritize(entity.getPrioritize())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .start(entity.getStart())
                .due(entity.getDue())
                .status(entity.isStatus())
                .categoryId(categoryId)
                .isRecurring(entity.isRecurring())
                .recurrenceInterval(entity.getRecurrenceInterval())
                .build();
    }
}
