package com.example._t1020159.viewcontroller;

import com.example._t1020159.dto.request.ItemFormRequestDTO;
import com.example._t1020159.dto.response.CategoriesResponseDTO;
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

    // 1. Hiển thị Form tạo mới
    @GetMapping("/new")
    public String showNewItemForm(Model model) {
        model.addAttribute("item", new ItemFormRequestDTO());
        model.addAttribute("categories", categoriesService.getAllCategories());
        return "item-form";
    }

    // 2. Hiển thị Form chỉnh sửa
    @GetMapping("/edit/{id}")
    public String showEditItemForm(@PathVariable Long id, Model model) {
        // Lấy Item hiện tại
        ItemWithAlertResponseDTO itemDto = itemService.getItemById(id);

        // Map sang DTO Form (Lưu ý: BỎ qua dueDate, chỉ lấy dueTime)
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
                // --- LOGIC MAPPING NGÀY GIỜ MỚI ---
                .startDate(itemDto.getStart() != null ? itemDto.getStart().toLocalDate() : null)
                .startTime(itemDto.getStart() != null ? itemDto.getStart().toLocalTime() : null)
                // .dueDate(...) -> ĐÃ XÓA VÌ KHÔNG DÙNG NỮA
                .dueTime(itemDto.getDue() != null ? itemDto.getDue().toLocalTime() : null)
                .build();

        model.addAttribute("item", formDto);
        model.addAttribute("categories", categoriesService.getAllCategories());

        // Dùng chung view item-form để đỡ phải tạo nhiều file
        return "item-form";
    }

    // 3. Xử lý Cập nhật
    @PostMapping("/edit/{id}")
    public String updateItem(@PathVariable Long id, @ModelAttribute("item") ItemFormRequestDTO formDto) {
        itemService.updateItemFromForm(id, formDto);
        return "redirect:/items/showview";
    }

    // 4. Xử lý Tạo mới
    @PostMapping("/create")
    public String createItem(@ModelAttribute("item") ItemFormRequestDTO formDto) {
        itemService.createItemFromForm(formDto);
        return "redirect:/items/showview";
    }

    // 5. Xử lý Xóa
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public String deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return "redirect:/items/showview";
    }

    // 6. Xem danh sách
    @GetMapping("/showview")
    public String showAllItems(Model model) {
        // Gọi hàm getAllItems (đã tích hợp logic tự động cập nhật ngày trễ)
        List<ItemWithAlertResponseDTO> items = itemService.getAllItems(null, null);
        model.addAttribute("items", items);
        return "items";
    }

    // 7. Lọc theo ngày
    @PostMapping("/filterByDate")
    public String filterItemsByDate(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        if (date == null) {
            return "redirect:/items/showview";
        }

        List<ItemWithAlertResponseDTO> items = itemService.getItemsContainingDate(date);
        model.addAttribute("items", items);
        model.addAttribute("selectedDate", date.toString());

        return "items";
    }

    // 8. Đảo trạng thái (Hoàn thành <-> Chưa)
    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Long id) {
        itemService.toggleItemStatus(id);
        return "redirect:/items/showview";
    }
}