package com.example._t1020159.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description; // mô tả

    private LocalDateTime start;

    //thêm unique cho due và start
    private LocalDateTime due; //ngày hết hạn

    private boolean status;// trạng thái

    private Long prioritize; // độ ưu tiên

    private boolean isRecurring;

    @Column(nullable = true)
    private String recurrenceInterval;


    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoriesEntity category;

    @Column(name = "attachments", nullable = true)
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttachmentsEntity> attachments;

    private String message; //nội dung tin nhắn

    private Integer alertBefore; // thông báo trước ...phút

    private boolean isSent;//đánh dấu đã gửi hay chưa




}
