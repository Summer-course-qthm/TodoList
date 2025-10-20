package com.example._t1020159.service;

import com.example._t1020159.dto.response.AttachmentsResponseDTO;
import com.example._t1020159.entity.AttachmentsEntity;
import com.example._t1020159.entity.ItemEntity;
import com.example._t1020159.repository.AttachmentsRepository;
import com.example._t1020159.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.out;

@Service
public class AttachmentService { // Tên class Service

    @Autowired
    private AttachmentsRepository attachmentsRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private GoogleDriveService googleDriveService;

    // Lớp nội bộ để chứa dữ liệu tải về
    @Data
    @AllArgsConstructor
    public static class FileDownloadData {
        private Resource resource;
        private String filename;
        private String contentType;
    }

    // Mapper hỗ trợ
    private AttachmentsResponseDTO mapToResponseDTO(AttachmentsEntity entity) {
        return AttachmentsResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .link(entity.getLink())
                .itemId(entity.getItem().getId())
                .build();
    }

    /**
     * 1. Tải file lên Google Drive, lưu URL vào CSDL (STORE / CREATE).
     * Phương thức này được Controller gọi.
     */
    @Transactional
    public AttachmentsResponseDTO storeAttachment(MultipartFile file, Long itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("Item ID is required for attachment.");
        }
        ItemEntity item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Item không tồn tại!"));

        try {

            String fileUrl = googleDriveService.uploadFile(file);

            AttachmentsEntity attachment = AttachmentsEntity.builder()
                    .item(item)
                    .name(file.getOriginalFilename())
                    .link(fileUrl)
                    .build();

            AttachmentsEntity savedAttachment = attachmentsRepository.save(attachment);
            System.out.println("Uploading file: ");

            return mapToResponseDTO(savedAttachment);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi tải file lên Google Drive!", e);
        }

    }

    /** 2. Lấy tất cả Attachments theo Item ID. */
    public List<AttachmentsResponseDTO> getAttachmentsByItemId(Long itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("Item ID must be provided.");
        }
        return attachmentsRepository.findByItemId(itemId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /** 3. Xóa file trên Google Drive và trong CSDL (DELETE). */
    @Transactional
    public String deleteAttachment(Long attachmentId) {
        AttachmentsEntity attachment = attachmentsRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment không tồn tại!"));
        try {
            // LƯU Ý: googleDriveService.deleteFile(fileId) cần File ID
            String fileId = attachment.getLink();
            googleDriveService.deleteFile(fileId); // Xóa trên Drive

            attachmentsRepository.delete(attachment); // Xóa trong CSDL

            return "Đã xóa attachment thành công: " + attachment.getName();
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xóa file trên Google Drive!", e);
        }
    }

    /** 4. Đổi tên file (RENAME / UPDATE). */
    @Transactional
    public AttachmentsResponseDTO renameAttachment(Long attachmentId, String newName) {
        AttachmentsEntity attachment = attachmentsRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment không tồn tại!"));

        // Cần thêm logic gọi GoogleDriveService để đổi tên trên Drive nếu cần.

        attachment.setName(newName);

        AttachmentsEntity updatedAttachment = attachmentsRepository.save(attachment);
        return mapToResponseDTO(updatedAttachment);
    }

    /** 5. Tải file từ Google Drive (DOWNLOAD). */
    public FileDownloadData downloadAttachment(Long attachmentId) {
        try {
            AttachmentsEntity attachment = attachmentsRepository.findById(attachmentId)
                    .orElseThrow(() -> new EntityNotFoundException("Attachment không tồn tại!"));

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            googleDriveService.downloadFile(attachment.getLink(), outputStream);

            Resource resource = new ByteArrayResource(outputStream.toByteArray());

            return new FileDownloadData(resource, attachment.getName(), attachment.getType());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi tải file từ Google Drive!", e);
        }
    }
}