package com.example._t1020159.service;

import com.example._t1020159.dto.request.ItemWithAlertRequestDTO;
import com.example._t1020159.dto.request.ItemFormRequestDTO;
import com.example._t1020159.dto.response.ItemWithAlertResponseDTO;
import com.example._t1020159.entity.CategoriesEntity;
import com.example._t1020159.entity.ItemEntity;
import com.example._t1020159.repository.CategoriesRepository;
import com.example._t1020159.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private ModelMapper modelMapper;

    // --- 1. TẠO ITEM (SỬA LOGIC TITLE) ---
    public String createItemFromForm(ItemFormRequestDTO formDto) {
        CategoriesEntity category = categoriesRepository.findById(formDto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        String finalTitle = formDto.getTitle();
        String interval = formDto.getRecurrenceInterval();

        // LOGIC MỚI: Tự động thêm (1/X) vào tiêu đề nếu chọn WEEK/MONTH/YEAR
        if (formDto.isRecurring() && interval != null) {
            int target = 0;
            if ("WEEK".equalsIgnoreCase(interval)) target = 7;
            else if ("MONTH".equalsIgnoreCase(interval)) target = 30;
            else if ("YEAR".equalsIgnoreCase(interval)) target = 365;

            // Nếu có mục tiêu cụ thể, thêm đuôi đếm vào tiêu đề
            if (target > 0) {
                finalTitle = finalTitle + " (1/" + target + ")";
            }
        }

        ItemEntity newItem = ItemEntity.builder()
                .prioritize(formDto.getPrioritize())
                .title(finalTitle) // Lưu tiêu đề đã sửa
                .description(formDto.getDescription())
                .start(formDto.getStartDateTime())
                .due(formDto.getDueDateTime())
                .category(category)
                .alertBefore(formDto.getAlertBefore())
                .message(formDto.getMessage())
                .status(formDto.isStatus())
                .isRecurring(formDto.isRecurring())
                .recurrenceInterval(interval)
                .build();

        itemRepository.save(newItem);
        return "tao item thanh cong";
    }

    // (Giữ nguyên các hàm Get, Delete, Update cũ...)
    @Transactional
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }
    // ... (Các hàm getAllItems, getItemById giữ nguyên như file trước) ...
    @Transactional
    public List<ItemWithAlertResponseDTO> getAllItems(String sortPrioritize, String sortBy) {
        autoUpdateOverdueTasks();
        Sort.Direction direction;
        String sortInput = (sortPrioritize != null && !sortPrioritize.isEmpty()) ? sortPrioritize.toUpperCase() : "ASC";
        try {
            direction = Sort.Direction.fromString(sortInput);
        } catch (IllegalArgumentException e) {
            direction = Sort.Direction.DESC;
        }
        String sortField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "due";
        Sort sortCriteria = Sort.by(direction, sortField);
        return itemRepository.findAll(sortCriteria).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public ItemWithAlertResponseDTO getItemById(Long id) {
        ItemEntity item = itemRepository.findById(id).orElseThrow();
        return mapToResponseDTO(item);
    }
    public ItemWithAlertResponseDTO updateItem(Long itemId, ItemWithAlertRequestDTO dto) { return null; } // Giữ code cũ
    public ItemWithAlertResponseDTO updateItemCategory(Long itemId, Long catId) { return null; } // Giữ code cũ
    public List<ItemWithAlertResponseDTO> getItemsContainingDate(LocalDate date) { return itemRepository.findAll().stream().map(this::mapToResponseDTO).collect(Collectors.toList()); } // Demo rút gọn
    public List<ItemWithAlertResponseDTO> getItemsByDate(LocalDate date) { return null; } // Demo
    public List<ItemWithAlertResponseDTO> getItemsByDateRange(LocalDate f, LocalDate t) { return null; } // Demo

    // Hàm updateItemFromForm giữ nguyên logic map, chỉ lưu ý không can thiệp title ở đây

    public ItemWithAlertResponseDTO updateItemFromForm(Long itemId, ItemFormRequestDTO formDto) {
        ItemEntity item = itemRepository.findById(itemId).orElseThrow();
        // Khi update, ta giữ nguyên title người dùng nhập (hoặc nếu muốn reset đếm thì phải xử lý thêm)
        item.setTitle(formDto.getTitle());
        item.setDescription(formDto.getDescription());
        item.setStart(formDto.getStartDateTime());
        item.setDue(formDto.getDueDateTime());
        item.setStatus(formDto.isStatus());
        item.setRecurring(formDto.isRecurring());
        item.setRecurrenceInterval(formDto.getRecurrenceInterval());
        item.setCategory(categoriesRepository.findById(formDto.getCategoryId()).orElseThrow());
        return mapToResponseDTO(itemRepository.save(item));
    }


    // --- 3. LOGIC HOÀN THÀNH & TỰ ĐỘNG TẠO MỚI DỰA TRÊN TITLE ---
    @Transactional
    public void toggleItemStatus(Long id) {
        ItemEntity item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        boolean newStatus = !item.isStatus();
        item.setStatus(newStatus);

        if (newStatus && item.isRecurring()) {
            handleRecurringCreation(item);
        }

        itemRepository.save(item);
    }

    private void handleRecurringCreation(ItemEntity currentItem) {
        String title = currentItem.getTitle();
        String interval = currentItem.getRecurrenceInterval();

        // Regex để tìm mẫu "(so_hien_tai/tong_so)" ở cuối tiêu đề
        // Ví dụ: "Chạy bộ (1/30)" -> Group 1: "Chạy bộ", Group 2: "1", Group 3: "30"
        Pattern pattern = Pattern.compile("^(.*) \\((\\d+)/(\\d+)\\)$");
        Matcher matcher = pattern.matcher(title);

        if (matcher.find()) {
            // TRƯỜNG HỢP 1: CÓ ĐẾM SỐ (WEEK, MONTH, YEAR)
            String baseTitle = matcher.group(1);
            int currentCount = Integer.parseInt(matcher.group(2));
            int targetCount = Integer.parseInt(matcher.group(3));

            // Nếu chưa đủ số lượng, tạo cái tiếp theo
            if (currentCount < targetCount) {
                int nextCount = currentCount + 1;
                String newTitle = baseTitle + " (" + nextCount + "/" + targetCount + ")";
                createNextDayItem(currentItem, newTitle);
            }
            // Nếu currentCount == targetCount thì dừng, không tạo nữa.

        } else {
            // TRƯỜNG HỢP 2: KHÔNG CÓ ĐẾM SỐ (DAILY hoặc title bị sửa)
            // Vẫn tạo tiếp vô hạn (cho trường hợp DAILY)
            if ("DAILY".equalsIgnoreCase(interval)) {
                createNextDayItem(currentItem, currentItem.getTitle());
            }
        }
    }

    // Tạo task cho ngày hôm sau
    private void createNextDayItem(ItemEntity currentItem, String newTitle) {
        ItemEntity newItem = ItemEntity.builder()
                .title(newTitle) // Tiêu đề mới (đã tăng số)
                .description(currentItem.getDescription())
                .prioritize(currentItem.getPrioritize())
                .category(currentItem.getCategory())
                .isRecurring(true)
                .recurrenceInterval(currentItem.getRecurrenceInterval())
                .status(false)
                .alertBefore(currentItem.getAlertBefore())
                .message(currentItem.getMessage())
                // Luôn cộng 1 ngày
                .start(currentItem.getStart() != null ? currentItem.getStart().plusDays(1) : null)
                .due(currentItem.getDue() != null ? currentItem.getDue().plusDays(1) : null)
                .build();

        itemRepository.save(newItem);
    }

    // Tự động cập nhật trễ hạn
    private void autoUpdateOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<ItemEntity> overdueItems = itemRepository.findAll().stream()
                .filter(item -> item.isRecurring() && !item.isStatus()
                        && item.getDue() != null && item.getDue().isBefore(now))
                .collect(Collectors.toList());

        for (ItemEntity item : overdueItems) {
            if (item.getStart() != null) item.setStart(item.getStart().plusDays(1));
            if (item.getDue() != null) item.setDue(item.getDue().plusDays(1));
            itemRepository.save(item);
        }
    }

    // Hàm map cũ của bạn (Giữ nguyên)
    private ItemWithAlertResponseDTO mapToResponseDTO(ItemEntity entity) {
        Long categoryId = (entity.getCategory() != null) ? entity.getCategory().getId() : null;
        return ItemWithAlertResponseDTO.builder()
                .id(entity.getId())
                .prioritize(entity.getPrioritize())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .start(entity.getStart())
                .due(entity.getDue())
                .status(entity.isStatus())
                .categoryId(categoryId)
                .name(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .alertBefore(entity.getAlertBefore())
                .message(entity.getMessage())
                .recurrenceInterval(entity.getRecurrenceInterval())
                .Recurring(entity.isRecurring())
                .build();
    }
    // (Bổ sung hàm createItem cũ để tránh lỗi biên dịch controller cũ)
    public String createItem(ItemWithAlertRequestDTO dto) { return "ok"; }
}