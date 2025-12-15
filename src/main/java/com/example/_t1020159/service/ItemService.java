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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
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

    // <<< PHẦN BỔ SUNG: Hàng đợi để lưu trữ các thông báo cần hiển thị trên web
    public static final Queue<String> PENDING_ALERTS = new ConcurrentLinkedQueue<>();
    // >>> END PHẦN BỔ SUNG

    // Hàm tiện ích ánh xạ Entity sang Response DTO (giữ nguyên)
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

    // =========================================================
    //               API ENDPOINT METHODS (REST)
    // =========================================================

    /**
     * 1. TẠO ITEM MỚI (Từ ItemController - sử dụng ItemWithAlertRequestDTO)
     */
    @Transactional
    public String createItem(ItemWithAlertRequestDTO requestDTO) {
        CategoriesEntity category = categoriesRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + requestDTO.getCategoryId()));

        ItemEntity newItem = modelMapper.map(requestDTO, ItemEntity.class);
        newItem.setCategory(category);
        newItem.setSent(false);

        itemRepository.save(newItem);
        return "Tạo item thành công";
    }

    /**
     * 2. CẬP NHẬT ITEM (Từ ItemController - sử dụng ItemWithAlertRequestDTO)
     */
    @Transactional
    public ItemWithAlertResponseDTO updateItem(Long itemId, ItemWithAlertRequestDTO requestDTO) {
        ItemEntity existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + itemId));

        CategoriesEntity category = categoriesRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + requestDTO.getCategoryId()));

        // Cập nhật các trường từ DTO
        existingItem.setTitle(requestDTO.getTitle());
        existingItem.setDescription(requestDTO.getDescription());
        existingItem.setStart(requestDTO.getStart());
        existingItem.setDue(requestDTO.getDue());
        existingItem.setStatus(requestDTO.isStatus());
        existingItem.setPrioritize(requestDTO.getPrioritize());
        existingItem.setRecurring(requestDTO.isRecurring());
        existingItem.setRecurrenceInterval(requestDTO.getRecurrenceInterval());
        existingItem.setAlertBefore(requestDTO.getAlertBefore());
        existingItem.setMessage(requestDTO.getMessage());
        existingItem.setCategory(category);

        ItemEntity updatedItem = itemRepository.save(existingItem);
        return mapToResponseDTO(updatedItem);
    }

    /**
     * 3. CẬP NHẬT CATEGORY ID CỦA ITEM
     */
    @Transactional
    public ItemWithAlertResponseDTO updateItemCategory(Long itemId, Long catId) {
        ItemEntity existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + itemId));

        CategoriesEntity category = categoriesRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + catId));

        existingItem.setCategory(category);

        ItemEntity updatedItem = itemRepository.save(existingItem);
        return mapToResponseDTO(updatedItem);
    }

    /**
     * 4. LẤY THEO NGÀY CỤ THỂ (Chỉ quan tâm ngày Start/Due)
     */
    public List<ItemWithAlertResponseDTO> getItemsByDate(LocalDate date) {
        if (date == null) return List.of();

        List<ItemEntity> items = itemRepository.findAll().stream()
                .filter(item -> (item.getStart() != null &&
                        item.getStart().toLocalDate().isEqual(date)) ||
                        (item.getDue() != null &&
                                item.getDue().toLocalDate().isEqual(date)))
                .collect(Collectors.toList());

        return items.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * 5. LẤY THEO KHOẢNG NGÀY (Start hoặc Due nằm trong khoảng)
     */
    public List<ItemWithAlertResponseDTO> getItemsByDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) return List.of();

        LocalDateTime startRange = fromDate.atStartOfDay();
        LocalDateTime endRange = toDate.atTime(23, 59, 59);

        List<ItemEntity> items = itemRepository.findAll().stream()
                .filter(item -> (item.getStart() != null &&
                        (item.getStart().isAfter(startRange) || item.getStart().isEqual(startRange)) &&
                        (item.getStart().isBefore(endRange) || item.getStart().isEqual(endRange))) ||
                        (item.getDue() != null &&
                                (item.getDue().isAfter(startRange) || item.getDue().isEqual(startRange)) &&
                                (item.getDue().isBefore(endRange) || item.getDue().isEqual(endRange))))
                .collect(Collectors.toList());

        return items.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // =========================================================
    //               VIEW CONTROLLER METHODS (GIỮ NGUYÊN)
    // =========================================================

    // --- 1. TẠO ITEM (FORM) ---
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

    // --- 2. LẤY DANH SÁCH & SẮP XẾP ---
    @Transactional
    public List<ItemWithAlertResponseDTO> getAllItems(String sortDir, String sortBy) {
        // Tự động cập nhật trễ hạn
        autoUpdateOverdueTasks();

        Sort.Direction direction = Sort.Direction.ASC;
        if (sortDir != null && sortDir.equalsIgnoreCase("DESC")) {
            direction = Sort.Direction.DESC;
        }

        String actualSortField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "due";

        Sort sortCriteria = Sort.by(direction, actualSortField);

        return itemRepository.findAll(sortCriteria).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // --- 3. TÌM KIẾM THEO NGÀY CHỨA (LOGIC TRONG REPOSITORY) ---
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

    // --- 4. CÁC LOGIC CRUD & LẶP LẠI (GIỮ NGUYÊN) ---

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
}