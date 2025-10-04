package com.example._t1020159.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String message; //nội dung tin nhắn

    private Integer alertBefore; // thông báo trước ...phút

    private boolean isSent;//đánh dấu đã gửi hay chưa

    @ManyToOne
    @JoinColumn(name = "item_id")
    private ItemEntity item;

}
