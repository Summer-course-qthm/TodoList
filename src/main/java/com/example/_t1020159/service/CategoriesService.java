package com.example._t1020159.service;

import com.example._t1020159.dto.request.CategoriesRequestDTO;
import com.example._t1020159.dto.response.CategoriesResponseDTO;
import com.example._t1020159.entity.CategoriesEntity;
import com.example._t1020159.repository.CategoriesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriesService {
    private final CategoriesRepository categoriesRepository;

    //lấy dữ liệu ra và tránh gây lôĩ trong vòng lặp
    private CategoriesResponseDTO mapToResponseDTO(CategoriesEntity entity) {
        return CategoriesResponseDTO.builder().id(entity.getId()).name(entity.getName()).build();
    }

    // 1. Tạo Category mới (Logic được đặt đúng chỗ)
    public CategoriesResponseDTO createCategory(CategoriesRequestDTO requestDTO) {

        CategoriesEntity newCategory = new CategoriesEntity();
        newCategory.setName(requestDTO.getName());

        CategoriesEntity savedCategory = categoriesRepository.save(newCategory);

        return mapToResponseDTO(savedCategory);
    }

    //2 xóa category theo id
    public void deleteCategory(Long id) {
        categoriesRepository.deleteById(id);
    }

    //3 lấy tất cả category
    public List<CategoriesResponseDTO> getAllCategories() {
        List<CategoriesEntity> categories = categoriesRepository.findAll();
        return categories.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    //4 cập nhật category
    public CategoriesResponseDTO updateCategory(Long id, CategoriesRequestDTO requestDTO) {
        CategoriesEntity existingCategory = categoriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        existingCategory.setName(requestDTO.getName());
        categoriesRepository.save(existingCategory);
        return mapToResponseDTO(existingCategory);
    }
}