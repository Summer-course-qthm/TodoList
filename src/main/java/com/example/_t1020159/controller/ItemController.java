package com.example._t1020159.controller;

import com.example._t1020159.dto.request.ItemRequestDTO;
import com.example._t1020159.dto.request.ItemWithAlertRequestDTO;
import com.example._t1020159.dto.request.UpdateCategoryRequestDTO;
import com.example._t1020159.dto.response.ItemResponseDTO;
import com.example._t1020159.dto.response.ItemWithAlertResponseDTO;
import com.example._t1020159.service.ItemService; // <<< Import class ItemService
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemService itemService; // Inject class ItemService trực tiếp

    // POST /api/items : Tạo Item mới
    @PostMapping
    public ResponseEntity<String> createItem(@RequestBody ItemWithAlertRequestDTO requestDTO) {
        String newItem = itemService.createItem(requestDTO);
        return ResponseEntity.ok(newItem);
    }
    // lấy tất cả item
    @GetMapping
    public ResponseEntity<List<ItemWithAlertResponseDTO>> getAllItems(
            @RequestParam(required = false, name = "prioritize", defaultValue = "ASC") String sortPrioritize,
            @RequestParam(required = false,name = "sortBy",defaultValue = "start") String sortBy) {
        List<ItemWithAlertResponseDTO> items = itemService.getAllItems(sortPrioritize, sortBy);
        return ResponseEntity.ok(items);
    }

    // lấy theo id item
    @GetMapping("/{id}")
    public ResponseEntity<ItemWithAlertResponseDTO> getItemById(@PathVariable Long id) {
        ItemWithAlertResponseDTO item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }

    //lấy theo ngày
    @GetMapping("/date")
    public ResponseEntity<List<ItemWithAlertResponseDTO>> getItemsByDate(@RequestParam("date") String dateStr) {
        LocalDate date = LocalDate.parse(dateStr); // ví dụ: 2025-10-16
        List<ItemWithAlertResponseDTO> items = itemService.getItemsByDate(date);
        return ResponseEntity.ok(items);
    }

    // Lấy theo khoảng ngày
    @GetMapping("/date-range")
    public ResponseEntity<List<ItemWithAlertResponseDTO>> getItemsByDateRange(
            @RequestParam("from") String fromStr,
            @RequestParam("to") String toStr) {

        LocalDate fromDate = LocalDate.parse(fromStr);
        LocalDate toDate = LocalDate.parse(toStr);

        List<ItemWithAlertResponseDTO> items = itemService.getItemsByDateRange(fromDate, toDate);
        return ResponseEntity.ok(items);
    }



    // xóa item
    /*@DeleteMapping("/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.ok("xóa thành công");
    }*/

    // cập nhật item
    @PutMapping("/{id}")
    public ResponseEntity<ItemWithAlertResponseDTO> updateItem(@PathVariable Long id, @RequestBody ItemWithAlertRequestDTO requestDTO) {
        ItemWithAlertResponseDTO updatedItem = itemService.updateItem(id, requestDTO);
        return ResponseEntity.ok(updatedItem);
    }

    // cập nhật id category của item
    @PatchMapping("/{id}/category")
    public ResponseEntity<ItemWithAlertResponseDTO> updateItemCategory(@PathVariable Long id, @RequestBody UpdateCategoryRequestDTO requestDTO) {
        ItemWithAlertResponseDTO updatedItem = itemService.updateItemCategory(id, requestDTO.getCategoryId());
        return ResponseEntity.ok(updatedItem);
    }


}