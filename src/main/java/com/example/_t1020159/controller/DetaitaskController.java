package com.example._t1020159.controller;

import com.example._t1020159.dto.request.ItemRequestDTO;
import com.example._t1020159.dto.response.ItemResponseDTO;
import com.example._t1020159.service.DetaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/detaitask")
public class DetaitaskController {

    @Autowired
    private DetaiService detaiService;

    // 1. Tạo mới detai-task
    @PostMapping
    public ResponseEntity<List<ItemResponseDTO>> createDetaiTask(@RequestBody ItemRequestDTO requestDTO) {
        List<ItemResponseDTO> newItems = detaiService.createDetaiTask(requestDTO);
        return ResponseEntity.ok(newItems);
    }

    // 2. Lấy tất cả detai-task
    @GetMapping
    public ResponseEntity<List<ItemResponseDTO>> getAllDetaiTasks() {
        List<ItemResponseDTO> items = detaiService.getAllDetaiTasks();
        return ResponseEntity.ok(items);
    }
}
