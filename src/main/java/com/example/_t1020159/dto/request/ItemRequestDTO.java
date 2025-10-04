package com.example._t1020159.dto.request;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequestDTO {

    private String title;

    private String description;

    private LocalDateTime start;

    private LocalDateTime due;

    private boolean status;

    private Long categoryId;

}
