package com.example._t1020159.viewcontroller;

import com.example._t1020159.dto.request.ItemFormRequestDTO;
import com.example._t1020159.dto.response.ItemWithAlertResponseDTO;
import com.example._t1020159.service.CategoriesService;
import com.example._t1020159.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity; // Nhớ import dòng này
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/items")
public class ItemViewController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private CategoriesService categoriesService;

    @GetMapping("/showview")
    public String showAllItems(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) boolean viewAll,
            Model model) {

        List<ItemWithAlertResponseDTO> items;

        // Vẫn giữ logic lấy alert tĩnh (nếu cần hiển thị lại khi F5)
        if (!ItemService.PENDING_ALERTS.isEmpty()) {
            List<String> alerts = new ArrayList<>();
            // Lưu ý: Chúng ta KHÔNG poll() ở đây nữa để dành cho API gọi,
            // hoặc nếu muốn hiển thị cả 2 nơi thì cần xử lý khéo hơn.
            // Nhưng để API hoạt động tốt nhất, logic poll() nên để ở API bên dưới.
            // Ở đây ta chỉ lấy danh sách item thôi.
        }

        if (date != null) {
            items = itemService.getItemsContainingDate(date);
            model.addAttribute("selectedDate", date.toString());
            model.addAttribute("viewAll", false);
        } else {
            items = itemService.getAllItems(sortDir, sortBy);
            model.addAttribute("selectedDate", null);
            model.addAttribute("viewAll", true);
        }

        model.addAttribute("items", items);
        model.addAttribute("viewAll", viewAll);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir != null && sortDir.equals("ASC") ? "DESC" : "ASC");

        return "items";
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

    // --- API QUAN TRỌNG CHO AJAX GỌI ---
    @GetMapping("/api/pending-alerts")
    public ResponseEntity<List<String>> getPendingAlerts() {
        List<String> alerts = new ArrayList<>();
        String alert;
        // Lấy thông báo ra khỏi hàng đợi để gửi về client
        while ((alert = ItemService.PENDING_ALERTS.poll()) != null) {
            alerts.add(alert);
        }
        return ResponseEntity.ok(alerts);
    }
}