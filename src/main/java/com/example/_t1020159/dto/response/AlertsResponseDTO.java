package com.example._t1020159.dto.response;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertsResponseDTO {

    private String message;

    private Integer alertBefore;

    private Long itemId;

}
