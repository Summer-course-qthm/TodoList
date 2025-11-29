package com.example._t1020159.dto.request;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemFormRequestDTO {

    // Các trường cơ bản (không đổi)
    private Long id;
    private String title;
    private Long prioritize;
    private String description;
    private boolean status;
    private Long categoryId;
    private boolean recurring;
    private String recurrenceInterval;

    // Cảnh báo (không đổi)
    private Integer alertBefore;
    private String message;

    // TRƯỜNG NGÀY/GIỜ MỚI ĐỂ NHẬN DỮ LIỆU TÁCH RỜI TỪ FORM

    // Ngày Bắt đầu
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    // Giờ Bắt đầu
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime startTime;

    // Ngày Kết thúc
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;

    // Giờ Kết thúc
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime dueTime;

    /**
     * Phương thức tiện ích để ghép Ngày và Giờ thành LocalDateTime
     */
    public LocalDateTime getStartDateTime() {
        if (startDate == null || startTime == null) return null;
        return LocalDateTime.of(startDate, startTime);
    }

    public LocalDateTime getDueDateTime() {
        if (dueDate == null || dueTime == null) return null;
        return LocalDateTime.of(dueDate, dueTime);
    }
}