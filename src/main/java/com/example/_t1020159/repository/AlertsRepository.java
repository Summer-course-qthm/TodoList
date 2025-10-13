package com.example._t1020159.repository;

import com.example._t1020159.entity.AlertsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertsRepository extends JpaRepository<AlertsEntity, Long> {
}