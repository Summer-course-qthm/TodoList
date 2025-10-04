package com.example._t1020159.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentsResponseDTO {
    private Long id;

    private String name;

    private String type;

    private String link;

    private Long itemId;

}
