package com.example._t1020159.service;

import com.example._t1020159.entity.ItemEntity;
import com.example._t1020159.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertSchedulerService {

    private final ItemRepository itemRepository;

    /**
     * Chạy định kỳ 1 phút (hoặc theo cấu hình) để kiểm tra và gửi các cảnh báo sắp đến hạn.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkAndSendAlerts() {
        // 1. Lấy tất cả Item chưa hoàn thành (status = false)
        List<ItemEntity> activeItems = itemRepository.findAll().stream()
                .filter(item -> !item.isStatus())
                .toList();

        LocalDateTime now = LocalDateTime.now();
        System.out.println("--- Scheduler chạy lúc: " + now + " ---");

        for (ItemEntity item : activeItems) {

            // Chỉ xử lý các Item có thiết lập cảnh báo và chưa được gửi
            if (item.getAlertBefore() != null && item.getMessage() != null && !item.isSent()) {

                // 2. Xác định mốc thời gian cơ sở (Ưu tiên Due Time)
                LocalDateTime baseTime = item.getDue();

                if (baseTime == null) {
                    baseTime = item.getStart();
                }

                if (baseTime == null) {
                    continue; // Bỏ qua nếu không có mốc thời gian nào
                }

                // 3. Tính toán Thời điểm Cảnh báo
                // alertTime = baseTime - AlertBefore
                // Ví dụ: Due=20:30, AlertBefore=5p -> AlertTime = 20:25
                LocalDateTime alertTime = baseTime.minusMinutes(item.getAlertBefore());

                // 4. Kiểm tra nếu thời điểm cảnh báo đã đến hoặc đã qua
                if (now.isAfter(alertTime)) {

                    // --- GỬI CẢNH BÁO (LƯU VÀO HÀNG ĐỢI) ---

                    String alertMessage = String.format(
                            "🔔 [CẢNH BÁO] Công việc '%s' sắp đến hạn! Hạn chót: %s. Nội dung: %s",
                            item.getTitle(),
                            baseTime.toLocalTime(),
                            item.getMessage()
                    );
                    ItemService.PENDING_ALERTS.offer(alertMessage); // <<< Lưu vào hàng đợi tĩnh

                    System.out.println(">> Đã kích hoạt CẢNH BÁO cho Item ID: " + item.getId() + " (Lưu vào hàng đợi Web)");

                    // 5. Đánh dấu đã gửi
                    item.setSent(true);
                    itemRepository.save(item);
                }
            }
        }
    }
}