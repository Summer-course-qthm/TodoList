package com.example._t1020159.service;

import com.example._t1020159.dto.request.ItemRequestDTO;
import com.example._t1020159.dto.response.ItemResponseDTO;
import com.example._t1020159.entity.CategoriesEntity;
import com.example._t1020159.entity.ItemEntity;
import com.example._t1020159.repository.CategoriesRepository;
import com.example._t1020159.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; // <<< Chú thích @Service được đặt tại đây
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service // Spring sẽ quản lý class này
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    //tạo item mới
    //@Transactional //giúp rollback nếu 1 bước trong transactional bị lỗi
    public ItemResponseDTO createItem(ItemRequestDTO requestDTO) {
        CategoriesEntity category = categoriesRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        ItemEntity newItem = ItemEntity.builder()
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .start(requestDTO.getStart())
                .due(requestDTO.getDue())
                .status(requestDTO.isStatus())
                .category(category)
                .build();

        ItemEntity savedItem = itemRepository.save(newItem);

        return ItemResponseDTO.builder()
                .id(savedItem.getId())
                .title(savedItem.getTitle())
                .description(savedItem.getDescription())
                .start(savedItem.getStart())
                .due(savedItem.getDue())
                .status(savedItem.isStatus())
                .categoryId(savedItem.getCategory().getId())
                .build();
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
                .title(entity.getTitle())
                .description(entity.getDescription())
                .start(entity.getStart())
                .due(entity.getDue())
                .status(entity.isStatus())
                .categoryId(categoryId)
                .build();
    }
    public List<ItemResponseDTO> getAllItems() {
        List<ItemEntity> items = itemRepository.findAll();
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




}