// Trong ItemRepository.java
package com.example._t1020159.repository;

import com.example._t1020159.entity.ItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

    // Để tìm kiếm tất cả nhiệm vụ lặp lại (cho getAllDailyTasks)
    List<ItemEntity> findByIsRecurring(boolean isRecurring);

    // Để tìm kiếm theo chu kỳ cụ thể (cho createRecurringTasks)
    List<ItemEntity> findByIsRecurringAndRecurrenceInterval(boolean isRecurring, String recurrenceInterval);

}