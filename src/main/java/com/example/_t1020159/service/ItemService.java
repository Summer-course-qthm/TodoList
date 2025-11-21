package com.example._t1020159.service;

import com.example._t1020159.dto.request.ItemRequestDTO;
import com.example._t1020159.dto.request.ItemWithAlertRequestDTO;
import com.example._t1020159.dto.response.ItemResponseDTO;
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

    //tạo item mới
    //@Transactional //giúp rollback nếu 1 bước trong transactional bị lỗi
    public String createItem(ItemWithAlertRequestDTO itemWithAlertRequestDTO) {
        CategoriesEntity category = categoriesRepository.findById(itemWithAlertRequestDTO.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        if(itemWithAlertRequestDTO.isRecurring()) {
            ItemEntity newItem = ItemEntity.builder()
                    .prioritize(itemWithAlertRequestDTO.getPrioritize())
                    .title(itemWithAlertRequestDTO.getTitle())
                    .description(itemWithAlertRequestDTO.getDescription())
                    .start(itemWithAlertRequestDTO.getStart())
                    .due(itemWithAlertRequestDTO.getDue())
                    .status(itemWithAlertRequestDTO.isStatus())
                    .category(category)
                    .alertBefore(itemWithAlertRequestDTO.getAlertBefore())
                    .message(itemWithAlertRequestDTO.getMessage())
                    .recurrenceInterval(itemWithAlertRequestDTO.getRecurrenceInterval())
                    .build();
            ItemEntity savedItem = itemRepository.save(newItem);
        }
        else {
            ItemEntity newItem = ItemEntity.builder()
                    .prioritize(itemWithAlertRequestDTO.getPrioritize())
                    .title(itemWithAlertRequestDTO.getTitle())
                    .description(itemWithAlertRequestDTO.getDescription())
                    .start(itemWithAlertRequestDTO.getStart())
                    .due(itemWithAlertRequestDTO.getDue())
                    .status(itemWithAlertRequestDTO.isStatus())
                    .category(category)
                    .alertBefore(itemWithAlertRequestDTO.getAlertBefore())
                    .message(itemWithAlertRequestDTO.getMessage())
                    .build();
            ItemEntity savedItem = itemRepository.save(newItem);
        }

        return "tao item thanh cong";

    }
    /** 2. Xóa Item */
    @Transactional
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }

    //3 lấy tất cả item
    private ItemResponseDTO mapToResponseDTO(ItemEntity entity) {
        // Cần kiểm tra null cho Category để tránh lỗi khi Item không có Category
        Long categoryId = null;
        if (entity.getCategory() != null) {
            categoryId = entity.getCategory().getId();
        }

        return ItemResponseDTO.builder()
                .id(entity.getId())
                .prioritize(entity.getPrioritize())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .start(entity.getStart())
                .due(entity.getDue())
                .status(entity.isStatus())
                .categoryId(categoryId)
                .build();
    }
    public List<ItemResponseDTO> getAllItems(String sortPrioritize, String sortBy) {
        Sort.Direction direction;
        String sortField;

        String sortInput = (sortPrioritize != null && !sortPrioritize.isEmpty())
                ? sortPrioritize.toUpperCase() // ĐẢM BẢO CHUYỂN SANG HOA
                : "ASC";


        try {
            direction = Sort.Direction.fromString(sortInput);
        }//bắt lỗi khi truyền sai kiểu dữ liệu đối số
        catch (IllegalArgumentException e) {
            direction = Sort.Direction.DESC;
        }
        //đối số là lúc mình sử dùng
        //tham số là lu mình tạo

        // XÁC ĐỊNH THUỘC TÍNH SẮP XẾP
        sortField = (sortBy != null && !sortBy.isEmpty()) ? sortBy : "due";

        // tạo đối tượng sắp xếp
        Sort sortCriteria  = Sort.by(direction, sortField);

        List<ItemEntity> items = itemRepository.findAll(sortCriteria);

        return items.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    //4 lấy theo id item
    public ItemResponseDTO getItemById(Long id) {
        ItemEntity item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));
        return mapToResponseDTO(item);
    }
    //5 cập nhật item
    public ItemResponseDTO updateItem(Long itemId, ItemRequestDTO requestDTO) {
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));
        item.setPrioritize(requestDTO.getPrioritize());
        item.setTitle(requestDTO.getTitle());
        item.setDescription(requestDTO.getDescription());
        item.setStart(requestDTO.getStart());
        item.setDue(requestDTO.getDue());
        item.setStatus(requestDTO.isStatus());

        ItemEntity updatedItem = itemRepository.save(item);
        return mapToResponseDTO(updatedItem);
    }

    //6 sửa id category trong item
    public ItemResponseDTO updateItemCategory(Long itemId, Long categoryId) {
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item not found")); // Tìm Item theo itemId
        CategoriesEntity category = categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found")); // Tìm Category theo categoryId
        item.setCategory(category); // Cập nhật Category cho Item
        ItemEntity updatedItem = itemRepository.save(item); // Lưu Item đã cập nhật
        return mapToResponseDTO(updatedItem); // Trả về Item đã cập nhật dưới dạng DTO
    }

    //7 get theo thời gian
    public List<ItemResponseDTO> getItemsByDate(LocalDate date) {
        return itemRepository.findAll().stream()
                .filter(item -> item.getStart() != null &&
                        item.getStart().toLocalDate().equals(date))
                .map(item -> modelMapper.map(item, ItemResponseDTO.class))
                .collect(Collectors.toList());
    }

    //8 get theo khoảng thời gian
    public List<ItemResponseDTO> getItemsByDateRange(LocalDate from, LocalDate to) {
        return itemRepository.findAll().stream()
                .filter(item -> item.getStart() != null &&
                        !item.getStart().toLocalDate().isBefore(from) &&
                        !item.getStart().toLocalDate().isAfter(to))
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

}