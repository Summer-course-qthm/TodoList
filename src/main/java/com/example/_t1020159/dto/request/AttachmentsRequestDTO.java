package com.example._t1020159.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentsRequestDTO {

    private String name;

    private String type;

    private String link;

    private Long itemId;

}
