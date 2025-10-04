package com.example._t1020159.dto.request;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertsRequestDTO {

    private String message;

    private Integer alertBefore;

    private boolean isSent;

    private Long itemId;

}
