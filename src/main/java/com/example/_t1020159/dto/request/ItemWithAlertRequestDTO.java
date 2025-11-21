package com.example._t1020159.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemWithAlertRequestDTO {

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
