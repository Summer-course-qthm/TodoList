package com.example._t1020159.repository;

import com.example._t1020159.entity.AttachmentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentsRepository extends JpaRepository<AttachmentsEntity, Long> {

    List<AttachmentsEntity> findByItemId(Long itemId);
}