package com.example._t1020159.dto.response;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponseDTO {

    private Long id;

    private Long prioritize;

    private String title;

    private String description;

    private LocalDateTime start;

    private LocalDateTime due;

    private boolean status;

    private Long categoryId;
}
