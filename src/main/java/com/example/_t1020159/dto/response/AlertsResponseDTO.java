package com.example._t1020159.dto.response;

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
public class AlertsResponseDTO {

    private Long id;

    private String message;

    // Số phút báo trước/sau Start (ví dụ: 5 phút sau Start)
    private Integer alertBefore;

    private boolean isSent;

    private Long itemId;
}