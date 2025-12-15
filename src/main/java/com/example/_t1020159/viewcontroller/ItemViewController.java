package com.example._t1020159.viewcontroller;

import com.example._t1020159.dto.request.ItemFormRequestDTO;
import com.example._t1020159.dto.response.ItemWithAlertResponseDTO;
import com.example._t1020159.service.CategoriesService;
import com.example._t1020159.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/items")
public class ItemViewController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private CategoriesService categoriesService;

    // --- HÀM QUAN TRỌNG NHẤT: HIỂN THỊ + TÌM KIẾM + SẮP XẾP ---
    @GetMapping("/showview")
    public String showAllItems(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, defaultValue = "start") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDir,
            @RequestParam(required = false, defaultValue = "false") boolean viewAll,
            Model model) {

        List<ItemWithAlertResponseDTO> items;
        LocalDate finalDate = date;

        // <<< PHẦN BỔ SUNG: XỬ LÝ CẢNH BÁO TỪ SCHEDULER
        if (!ItemService.PENDING_ALERTS.isEmpty()) {
            List<String> alerts = new java.util.ArrayList<>();
            String alert;
            // Lấy tất cả thông báo đang chờ
            while ((alert = ItemService.PENDING_ALERTS.poll()) != null) {
                alerts.add(alert);
            }
            model.addAttribute("alerts", alerts);
        }
        // >>> END PHẦN BỔ SUNG

        // LOGIC MỚI:
        // 1. Nếu người dùng muốn xem tất cả (viewAll=true) -> Lấy hết.
        if (viewAll) {
            items = itemService.getAllItems(sortDir, sortBy);
            model.addAttribute("selectedDate", null);
        }
        // 2. Ngược lại (mặc định):
        else {
            // Nếu không chọn ngày cụ thể -> TỰ ĐỘNG LẤY HÔM NAY
            if (date == null) {
                finalDate = LocalDate.now();
            }
            // Lấy danh sách theo ngày
            items = itemService.getItemsContainingDate(finalDate);
            model.addAttribute("selectedDate", finalDate.toString());
        }

        model.addAttribute("items", items);

        // FIX LỖI: Đảm bảo biến viewAll luôn được truyền vào Model
        model.addAttribute("viewAll", viewAll);

        // Truyền tham số để View biết đang sort theo gì
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("ASC") ? "DESC" : "ASC");

        return "items";
    }

    // Form lọc cũ -> Chuyển hướng sang Showview để dùng GET
    @PostMapping("/filterByDate")
    public String filterItemsByDate(@RequestParam("date") String dateStr) {
        return "redirect:/items/showview?date=" + dateStr;
    }

    // --- CÁC HÀM CRUD CƠ BẢN ---

    @GetMapping("/new")
    public String showNewItemForm(Model model) {
        model.addAttribute("item", new ItemFormRequestDTO());
        model.addAttribute("categories", categoriesService.getAllCategories());
        return "item-form";
    }

    @PostMapping("/create")
    public String createItem(@ModelAttribute("item") ItemFormRequestDTO formDto) {
        itemService.createItemFromForm(formDto);
        return "redirect:/items/showview";
    }

    @GetMapping("/edit/{id}")
    public String showEditItemForm(@PathVariable Long id, Model model) {
        ItemWithAlertResponseDTO itemDto = itemService.getItemById(id);
        ItemFormRequestDTO formDto = ItemFormRequestDTO.builder()
                .id(itemDto.getId())
                .title(itemDto.getTitle())
                .description(itemDto.getDescription())
                .prioritize(itemDto.getPrioritize())
                .status(itemDto.isStatus())
                .categoryId(itemDto.getCategoryId())
                .recurring(itemDto.isRecurring())
                .recurrenceInterval(itemDto.getRecurrenceInterval())
                .alertBefore(itemDto.getAlertBefore())
                .message(itemDto.getMessage())
                .startDate(itemDto.getStart() != null ? itemDto.getStart().toLocalDate() : null)
                .startTime(itemDto.getStart() != null ? itemDto.getStart().toLocalTime() : null)
                .dueTime(itemDto.getDue() != null ? itemDto.getDue().toLocalTime() : null)
                .build();
        model.addAttribute("item", formDto);
        model.addAttribute("categories", categoriesService.getAllCategories());
        return "item-form";
    }

    @PostMapping("/edit/{id}")
    public String updateItem(@PathVariable Long id, @ModelAttribute("item") ItemFormRequestDTO formDto) {
        itemService.updateItemFromForm(id, formDto);
        return "redirect:/items/showview";
    }

    @RequestMapping(value = "/delete/{id}", method = {RequestMethod.DELETE, RequestMethod.POST})
    public String deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return "redirect:/items/showview";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Long id) {
        itemService.toggleItemStatus(id);
        return "redirect:/items/showview";
    }
}