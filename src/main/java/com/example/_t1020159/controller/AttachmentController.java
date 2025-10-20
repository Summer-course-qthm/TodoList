package com.example._t1020159.controller;

import com.example._t1020159.dto.response.AttachmentsResponseDTO;
import com.example._t1020159.service.AttachmentService;
import com.example._t1020159.service.AttachmentService.FileDownloadData; // Lớp nội bộ cho Download
import com.example._t1020159.service.GoogleDriveService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final GoogleDriveService googleDriveService; // Giữ lại service này

    // 1. TẠO ATTACHMENT (UPLOAD FILE)
    // POST /attachments
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<AttachmentsResponseDTO> createAttachment(
            @RequestPart("file") MultipartFile file,
            @RequestParam("itemId") Long itemId) {

        try {
            // GỌI ĐÚNG PHƯƠNG THỨC: storeAttachment (Tải lên Drive và lưu DB)
            AttachmentsResponseDTO newAttachment = attachmentService.storeAttachment(file, itemId);
            return new ResponseEntity<>(newAttachment, HttpStatus.CREATED);

        } catch (EntityNotFoundException e) {
            // 404: Item cha không tồn tại
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalArgumentException e) {
            // 400: Dữ liệu thiếu (ví dụ: itemId = null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (RuntimeException e) {
            // 500: Lỗi IO hoặc Drive API
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    // 2. LẤY TẤT CẢ ATTACHMENT THEO ITEM ID
    // GET /attachments/item/{itemId}
    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<AttachmentsResponseDTO>> getAttachmentsByItemId(@PathVariable Long itemId) {
        List<AttachmentsResponseDTO> attachments = attachmentService.getAttachmentsByItemId(itemId);
        return ResponseEntity.ok(attachments);
    }

    // 3. CẬP NHẬT/ĐỔI TÊN ATTACHMENT
    // PUT /attachments/{id}/rename
    @PutMapping("/{id}/rename")
    public ResponseEntity<AttachmentsResponseDTO> renameAttachment(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        String newName = payload.get("newName");

        AttachmentsResponseDTO updatedAttachment = attachmentService.renameAttachment(id, newName);
        return ResponseEntity.ok(updatedAttachment);
    }

    // 4. XÓA ATTACHMENT
    // DELETE /attachments/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAttachment(@PathVariable Long id) {
        try {
            String message = attachmentService.deleteAttachment(id);
            return ResponseEntity.ok(message);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Attachment không tồn tại.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa file trên Drive.");
        }
    }

    // 5. DOWNLOAD FILE
    // GET /attachments/download/{id}
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            FileDownloadData data = attachmentService.downloadAttachment(id);

            // Cấu hình header để trình duyệt tải về
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(data.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + data.getFilename() + "\"")
                    .body(data.getResource());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}