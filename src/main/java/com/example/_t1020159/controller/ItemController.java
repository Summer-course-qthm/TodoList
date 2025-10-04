package com.example._t1020159.controller;

import com.example._t1020159.dto.request.ItemRequestDTO;
import com.example._t1020159.dto.response.ItemResponseDTO;
import com.example._t1020159.service.ItemService; // <<< Import class ItemService
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService; // Inject class ItemService trực tiếp

    // POST /api/items : Tạo Item mới
    @PostMapping
    public ResponseEntity<ItemResponseDTO> createItem(@RequestBody ItemRequestDTO requestDTO) {
        ItemResponseDTO newItem = itemService.createItem(requestDTO);
        return ResponseEntity.ok(newItem);
    }
    // lấy tất cả item
    @GetMapping
    public ResponseEntity<List<ItemResponseDTO>> getAllItems() {
        List<ItemResponseDTO> items = itemService.getAllItems();
        return ResponseEntity.ok(items);
    }

    // lấy theo id item
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDTO> getItemById(@PathVariable Long id) {
        ItemResponseDTO item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }

    // xóa item
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.ok("xóa thành công");
    }


}