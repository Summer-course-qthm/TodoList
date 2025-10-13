package com.example._t1020159.controller;

import com.example._t1020159.dto.request.CategoriesRequestDTO;
import com.example._t1020159.dto.response.CategoriesResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final com.example._t1020159.service.CategoriesService categoriesService; // Inject class CategoriesService trực tiếp

    // POST /api/categories : Tạo Category mới
    @PostMapping
    public ResponseEntity<CategoriesResponseDTO> createCategory(@RequestBody CategoriesRequestDTO requestDTO) {
        CategoriesResponseDTO newCategory = categoriesService.createCategory(requestDTO);
        return ResponseEntity.ok(newCategory);
    }

    // lấy tất cả category
    @GetMapping
    public ResponseEntity<List<CategoriesResponseDTO>> getAllCategories() {
        List<CategoriesResponseDTO> categories = categoriesService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // xóa category
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoriesService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    //cập nhật category
    @PostMapping("/{id}")
    public ResponseEntity<CategoriesResponseDTO> updateCategory(@PathVariable Long id, @RequestBody CategoriesRequestDTO requestDTO) {
        CategoriesResponseDTO updatedCategory = categoriesService.updateCategory(id, requestDTO);
        return ResponseEntity.ok(updatedCategory);
    }


}
