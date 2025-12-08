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

    // --- 1. TẠO ITEM (GIỮ NGUYÊN LOGIC CỦA BẠN) ---
    public String createItemFromForm(ItemFormRequestDTO formDto) {
        CategoriesEntity category = categoriesRepository.findById(formDto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        String finalTitle = formDto.getTitle();
        String interval = formDto.getRecurrenceInterval();

        if (formDto.isRecurring() && interval != null) {
            int target = 0;
            if ("WEEK".equalsIgnoreCase(interval)) target = 7;
            else if ("MONTH".equalsIgnoreCase(interval)) target = 30;
            else if ("YEAR".equalsIgnoreCase(interval)) target = 365;

            if (target > 0) {
                finalTitle = finalTitle + " (1/" + target + ")";
            }
        }

        ItemEntity newItem = ItemEntity.builder()
                .prioritize(formDto.getPrioritize())
                .title(finalTitle)
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

    // --- 2. LẤY DANH SÁCH & SẮP XẾP (ĐÃ SỬA) ---
    @Transactional
    public List<ItemWithAlertResponseDTO> getAllItems(String sortDir, String sortBy) {
        // Tự động cập nhật trễ hạn
        autoUpdateOverdueTasks();

        // Xử lý hướng sắp xếp (ASC/DESC)
        Sort.Direction direction = Sort.Direction.ASC;
        if (sortDir != null && sortDir.equalsIgnoreCase("DESC")) {
            direction = Sort.Direction.DESC;
        }

        // Xử lý trường sắp xếp (Mặc định là 'due' - ngày kết thúc)
        String actualSortField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "due";

        Sort sortCriteria = Sort.by(direction, actualSortField);

        return itemRepository.findAll(sortCriteria).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // --- 3. TÌM KIẾM THEO NGÀY (ĐÃ SỬA GỌI REPO MỚI) ---
    public List<ItemWithAlertResponseDTO> getItemsContainingDate(LocalDate date) {
        if (date == null) return List.of();

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        // Gọi query tùy chỉnh trong Repository
        List<ItemEntity> items = itemRepository.findTasksContainingDate(startOfDay, endOfDay);

        return items.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // --- 4. CÁC LOGIC KHÁC (GIỮ NGUYÊN) ---
    @Transactional
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }

    public ItemWithAlertResponseDTO getItemById(Long id) {
        ItemEntity item = itemRepository.findById(id).orElseThrow();
        return mapToResponseDTO(item);
    }

    public ItemWithAlertResponseDTO updateItemFromForm(Long itemId, ItemFormRequestDTO formDto) {
        ItemEntity item = itemRepository.findById(itemId).orElseThrow();
        item.setTitle(formDto.getTitle());
        item.setDescription(formDto.getDescription());
        item.setStart(formDto.getStartDateTime());
        item.setDue(formDto.getDueDateTime());
        item.setStatus(formDto.isStatus());
        item.setRecurring(formDto.isRecurring());
        item.setRecurrenceInterval(formDto.getRecurrenceInterval());
        item.setCategory(categoriesRepository.findById(formDto.getCategoryId()).orElseThrow());

        // Cập nhật thêm các trường quan trọng
        item.setPrioritize(formDto.getPrioritize());
        item.setAlertBefore(formDto.getAlertBefore());
        item.setMessage(formDto.getMessage());

        return mapToResponseDTO(itemRepository.save(item));
    }

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
        Pattern pattern = Pattern.compile("^(.*) \\((\\d+)/(\\d+)\\)$");
        Matcher matcher = pattern.matcher(title);

        if (matcher.find()) {
            String baseTitle = matcher.group(1);
            int currentCount = Integer.parseInt(matcher.group(2));
            int targetCount = Integer.parseInt(matcher.group(3));
            if (currentCount < targetCount) {
                int nextCount = currentCount + 1;
                String newTitle = baseTitle + " (" + nextCount + "/" + targetCount + ")";
                createNextDayItem(currentItem, newTitle);
            }
        } else {
            if ("DAILY".equalsIgnoreCase(interval)) {
                createNextDayItem(currentItem, currentItem.getTitle());
            }
        }
    }

    private void createNextDayItem(ItemEntity currentItem, String newTitle) {
        ItemEntity newItem = ItemEntity.builder()
                .title(newTitle)
                .description(currentItem.getDescription())
                .prioritize(currentItem.getPrioritize())
                .category(currentItem.getCategory())
                .isRecurring(true)
                .recurrenceInterval(currentItem.getRecurrenceInterval())
                .status(false)
                .alertBefore(currentItem.getAlertBefore())
                .message(currentItem.getMessage())
                .start(currentItem.getStart() != null ? currentItem.getStart().plusDays(1) : null)
                .due(currentItem.getDue() != null ? currentItem.getDue().plusDays(1) : null)
                .build();
        itemRepository.save(newItem);
    }

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

    // Các hàm placeholder
    public String createItem(ItemWithAlertRequestDTO dto) { return "ok"; }
    public ItemWithAlertResponseDTO updateItem(Long itemId, ItemWithAlertRequestDTO dto) { return null; }
    public ItemWithAlertResponseDTO updateItemCategory(Long itemId, Long catId) { return null; }
    public List<ItemWithAlertResponseDTO> getItemsByDate(LocalDate date) { return null; }
    public List<ItemWithAlertResponseDTO> getItemsByDateRange(LocalDate f, LocalDate t) { return null; }
}