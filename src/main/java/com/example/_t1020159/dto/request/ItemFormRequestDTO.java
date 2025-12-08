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

    private Long id;
    private String title;
    private Long prioritize;
    private String description;
    private boolean status;
    private Long categoryId;

    // Logic lặp lại
    private boolean recurring;
    private String recurrenceInterval; // "DAILY", "WEEK"

    // Cảnh báo
    private Integer alertBefore;
    private String message;

    // --- CÁC TRƯỜNG THỜI GIAN ĐÃ SỬA ĐỔI ---

    // 1. Ngày thực hiện (Dùng chung cho cả Start và Due)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    // 2. Giờ bắt đầu
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime startTime;

    // 3. Giờ kết thúc (Bỏ dueDate, chỉ lấy giờ)
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime dueTime;

    /**
     * Ghép Ngày + Giờ Bắt đầu
     */
    public LocalDateTime getStartDateTime() {
        if (startDate == null || startTime == null) return null;
        return LocalDateTime.of(startDate, startTime);
    }

    /**
     * Logic mới: Ngày Kết thúc = Ngày Bắt đầu + Giờ Kết thúc
     */
    public LocalDateTime getDueDateTime() {
        if (startDate == null || dueTime == null) return null;
        return LocalDateTime.of(startDate, dueTime); // Vẫn dùng startDate
    }
}