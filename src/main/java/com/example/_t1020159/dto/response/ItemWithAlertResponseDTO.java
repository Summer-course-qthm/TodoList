package com.example._t1020159.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemWithAlertResponseDTO {

    private Long id;

    private String title;

    private LocalDateTime start;

    private LocalDateTime due;

    private Long prioritize;

    private String description;

    private boolean status;

    private Long categoryId;

    private String name;

    private boolean Recurring;

    private String recurrenceInterval; // DAILY, WEEK,

    // Số phút thông báo (âm nếu sau Start, dương nếu trước Due hoặc Start, tùy theo logic nền)
    private Integer alertBefore;

    // Nội dung tin nhắn
    private String message;
}
