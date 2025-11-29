package com.example._t1020159.service;

import com.example._t1020159.dto.request.ItemWithAlertRequestDTO;
import com.example._t1020159.dto.request.ItemFormRequestDTO; // <<< Import DTO mới
import com.example._t1020159.dto.response.ItemWithAlertResponseDTO;
import com.example._t1020159.entity.CategoriesEntity;
import com.example._t1020159.entity.ItemEntity;
import com.example._t1020159.repository.CategoriesRepository;
import com.example._t1020159.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service; // <<< Chú thích @Service được đặt tại đây
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service // Spring sẽ quản lý class này
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private ModelMapper modelMapper;

    // PHƯƠNG THỨC GỐC TẠO ITEM (GIỮ LẠI CHO API CONTROLLER)
    public String createItem(ItemWithAlertRequestDTO itemWithAlertRequestDTO) {
        CategoriesEntity category = categoriesRepository.findById(itemWithAlertRequestDTO.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        ItemEntity.ItemEntityBuilder builder = ItemEntity.builder()
                .prioritize(itemWithAlertRequestDTO.getPrioritize())
                .title(itemWithAlertRequestDTO.getTitle())
                .description(itemWithAlertRequestDTO.getDescription())
                .start(itemWithAlertRequestDTO.getStart())
                .due(itemWithAlertRequestDTO.getDue())
                .status(itemWithAlertRequestDTO.isStatus())
                .category(category)
                .alertBefore(itemWithAlertRequestDTO.getAlertBefore())
                .message(itemWithAlertRequestDTO.getMessage());

        if (itemWithAlertRequestDTO.isRecurring()) {
            builder.recurrenceInterval(itemWithAlertRequestDTO.getRecurrenceInterval());
            builder.isRecurring(true);
        } else {
            builder.isRecurring(false);
        }

        ItemEntity newItem = builder.build();
        itemRepository.save(newItem);

        return "tao item thanh cong";
    }

    /**
     * TẠO ITEM MỚI (Từ Form DTO - KHÔNG DÙNG JAVASCRIPT)
     */
    public String createItemFromForm(ItemFormRequestDTO formDto) {
        CategoriesEntity category = categoriesRepository.findById(formDto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        ItemEntity.ItemEntityBuilder builder = ItemEntity.builder()
                .prioritize(formDto.getPrioritize())
                .title(formDto.getTitle())
                .description(formDto.getDescription())
                // GHÉP NGÀY/GIỜ TRÊN SERVER
                .start(formDto.getStartDateTime())
                .due(formDto.getDueDateTime())
                // ... (các trường khác)
                .category(category)
                .alertBefore(formDto.getAlertBefore())
                .message(formDto.getMessage())
                .status(formDto.isStatus());

        if (formDto.isRecurring()) {
            builder.recurrenceInterval(formDto.getRecurrenceInterval());
            builder.isRecurring(true);
        } else {
            builder.isRecurring(false);
        }

        ItemEntity newItem = builder.build();
        itemRepository.save(newItem);

        return "tao item thanh cong";
    }

    /** 2. Xóa Item */
    @Transactional
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }

    //3 lấy tất cả item
    private ItemWithAlertResponseDTO mapToResponseDTO(ItemEntity entity) {
        // Cần kiểm tra null cho Category để tránh lỗi khi Item không có Category
        Long categoryId = null;
        if (entity.getCategory() != null) {
            categoryId = entity.getCategory().getId();
        }

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
                .build();
    }
    public List<ItemWithAlertResponseDTO> getAllItems(String sortPrioritize, String sortBy) {
        Sort.Direction direction;
        String sortField;

        String sortInput = (sortPrioritize != null && !sortPrioritize.isEmpty())
                ? sortPrioritize.toUpperCase()
                : "ASC";


        try {
            direction = Sort.Direction.fromString(sortInput);
        }
        catch (IllegalArgumentException e) {
            direction = Sort.Direction.DESC;
        }

        sortField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "due";

        Sort sortCriteria  = Sort.by(direction, sortField);

        List<ItemEntity> items = itemRepository.findAll(sortCriteria);

        return items.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    //4 lấy theo id item
    public ItemWithAlertResponseDTO getItemById(Long id) {
        ItemEntity item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));
        return mapToResponseDTO(item);
    }
    //5 cập nhật item (PHƯƠNG THỨC GỐC - GIỮ LẠI CHO API CONTROLLER)
    public ItemWithAlertResponseDTO updateItem(Long itemId, ItemWithAlertRequestDTO requestDTO) {
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        // Cập nhật các trường
        item.setPrioritize(requestDTO.getPrioritize());
        item.setTitle(requestDTO.getTitle());
        item.setDescription(requestDTO.getDescription());
        item.setStart(requestDTO.getStart());
        item.setDue(requestDTO.getDue());
        item.setStatus(requestDTO.isStatus());
        item.setAlertBefore(requestDTO.getAlertBefore());
        item.setMessage(requestDTO.getMessage());
        item.setRecurring(requestDTO.isRecurring());
        item.setRecurrenceInterval(requestDTO.getRecurrenceInterval());

        // Cập nhật Category nếu Category ID được truyền vào
        if (requestDTO.getCategoryId() != null) {
            CategoriesEntity category = categoriesRepository.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            item.setCategory(category);
        }

        ItemEntity updatedItem = itemRepository.save(item);
        return mapToResponseDTO(updatedItem);
    }

    /**
     * CẬP NHẬT ITEM (Từ Form DTO - KHÔNG DÙNG JAVASCRIPT)
     */
    public ItemWithAlertResponseDTO updateItemFromForm(Long itemId, ItemFormRequestDTO formDto) {
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        // Cập nhật các trường
        item.setPrioritize(formDto.getPrioritize());
        item.setTitle(formDto.getTitle());
        item.setDescription(formDto.getDescription());
        // GHÉP NGÀY/GIỜ TRÊN SERVER
        item.setStart(formDto.getStartDateTime());
        item.setDue(formDto.getDueDateTime());
        // ... (các trường khác)
        item.setStatus(formDto.isStatus());
        item.setAlertBefore(formDto.getAlertBefore());
        item.setMessage(formDto.getMessage());
        item.setRecurring(formDto.isRecurring());
        item.setRecurrenceInterval(formDto.getRecurrenceInterval());

        // Cập nhật Category nếu Category ID được truyền vào
        if (formDto.getCategoryId() != null) {
            CategoriesEntity category = categoriesRepository.findById(formDto.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            item.setCategory(category);
        }

        ItemEntity updatedItem = itemRepository.save(item);
        return mapToResponseDTO(updatedItem);
    }

    //6 sửa id category trong item
    public ItemWithAlertResponseDTO updateItemCategory(Long itemId, Long categoryId) {
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));
        CategoriesEntity category = categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        item.setCategory(category);
        ItemEntity updatedItem = itemRepository.save(item);
        return mapToResponseDTO(updatedItem);
    }

    //7 get theo thời gian (ĐÃ SỬA: Dùng logic chứa ngày)
    public List<ItemWithAlertResponseDTO> getItemsContainingDate(LocalDate selectedDate) {
        return itemRepository.findAll().stream()
                .filter(item -> {
                    if (item.getStart() == null || item.getDue() == null) {
                        return false;
                    }
                    LocalDate startDate = item.getStart().toLocalDate();
                    LocalDate dueDate = item.getDue().toLocalDate();

                    // Logic lọc: startDate <= selectedDate AND dueDate >= selectedDate
                    boolean isAfterOrEqualStart = !selectedDate.isBefore(startDate);
                    boolean isBeforeOrEqualDue = !selectedDate.isAfter(dueDate);

                    return isAfterOrEqualStart && isBeforeOrEqualDue;
                })
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Phương thức cũ (chỉ lọc theo start date) - Có thể xóa nếu không cần
    public List<ItemWithAlertResponseDTO> getItemsByDate(LocalDate date) {
        return itemRepository.findAll().stream()
                .filter(item -> item.getStart() != null &&
                        item.getStart().toLocalDate().equals(date))
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    //8 get theo khoảng thời gian
    public List<ItemWithAlertResponseDTO> getItemsByDateRange(LocalDate from, LocalDate to) {
        return itemRepository.findAll().stream()
                .filter(item -> item.getStart() != null &&
                        !item.getStart().toLocalDate().isBefore(from) &&
                        !item.getStart().toLocalDate().isAfter(to))
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
}