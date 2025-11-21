package com.example._t1020159.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertsRequestDTO {

    // Số phút thông báo (âm nếu sau Start, dương nếu trước Due hoặc Start, tùy theo logic nền)
    private Integer alertBefore;

    // Nội dung tin nhắn
    private String message;

    // ID của Item mà Alert này liên kết đến (BẮT BUỘC)
    private Long itemId;
}