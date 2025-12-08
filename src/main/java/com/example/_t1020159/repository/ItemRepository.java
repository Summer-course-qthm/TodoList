// Trong ItemRepository.java
package com.example._t1020159.repository;

import com.example._t1020159.entity.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

    // Để tìm kiếm tất cả nhiệm vụ lặp lại (cho getAllDailyTasks)
    List<ItemEntity> findByIsRecurring(boolean isRecurring);

    // Để tìm kiếm theo chu kỳ cụ thể (cho createRecurringTasks)
    List<ItemEntity> findByIsRecurringAndRecurrenceInterval(boolean isRecurring, String recurrenceInterval);

    @Query("SELECT i FROM ItemEntity i WHERE i.start <= :endOfDay AND i.due >= :startOfDay")
    List<ItemEntity> findTasksContainingDate(@Param("startOfDay") LocalDateTime startOfDay,
                                             @Param("endOfDay") LocalDateTime endOfDay);

}