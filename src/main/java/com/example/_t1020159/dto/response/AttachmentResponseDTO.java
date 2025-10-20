package com.example._t1020159.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AttachmentResponseDTO {

    private Long id;  // ID của Attachment

    private String name;

    private String link; // Link ảnh trả về

    private Long itemId;
}