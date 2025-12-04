package com.example._t1020159.viewcontroller;

import com.example._t1020159.dto.request.ItemWithAlertRequestDTO;
import com.example._t1020159.dto.request.ItemFormRequestDTO; // <<< Import DTO mới
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

    // 1. Phương thức hiển thị Form tạo Item
    @GetMapping("/new")
    public String showNewItemForm(Model model) {
        // Dùng DTO Form mới
        model.addAttribute("item", new ItemFormRequestDTO());

        // Lấy danh sách Category DTO và truyền vào Model
        List<CategoriesResponseDTO> categories = categoriesService.getAllCategories();
        model.addAttribute("categories", categories);

        return "item-form";
    }
    //2. lấy item theo id
    @GetMapping("/edit/{id}")
    public String showEditItemForm(@PathVariable Long id, Model model) {
        // Lấy Item theo ID
        ItemWithAlertResponseDTO itemDto = itemService.getItemById(id);

        // Map ItemResponseDTO sang ItemFormRequestDTO cho form (Cần xử lý mapping thủ công)
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
                // Tách LocalDateTime thành LocalDate và LocalTime
                .startDate(itemDto.getStart() != null ? itemDto.getStart().toLocalDate() : null)
                .startTime(itemDto.getStart() != null ? itemDto.getStart().toLocalTime() : null)
                .dueDate(itemDto.getDue() != null ? itemDto.getDue().toLocalDate() : null)
                .dueTime(itemDto.getDue() != null ? itemDto.getDue().toLocalTime() : null)
                .build();

        model.addAttribute("item", formDto);

        // Lấy danh sách Category DTO và truyền vào Model
        List<CategoriesResponseDTO> categories = categoriesService.getAllCategories();
        model.addAttribute("categories", categories);
        return "item-form2";
    }

    @PostMapping("/edit/{id}")
    // Dùng DTO Form mới và gọi Service mới
    public String updateItem(@PathVariable Long id, @ModelAttribute("item") ItemFormRequestDTO formDto) {
        itemService.updateItemFromForm(id, formDto);
        return "redirect:/items/showview";
    }

    // 2. Phương thức xử lý tạo Item
    @PostMapping("/create")
    // Dùng DTO Form mới và gọi Service mới
    public String createItem(@ModelAttribute("item") ItemFormRequestDTO formDto) {
        itemService.createItemFromForm(formDto);

        return "redirect:/items/showview";
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public String deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);

        return "redirect:/items/showview";
    }


    // 3. Phương thức Xem danh sách Item
    @GetMapping("/showview")
    public String showAllItems(Model model) {
        List<ItemWithAlertResponseDTO> items = itemService.getAllItems(null, null);
        model.addAttribute("items", items);
        return "items";
    }

    // 4. LỌC DANH SÁCH THEO NGÀY (Logic: selectedDate nằm trong [start, due])
    @PostMapping("/filterByDate")
    public String filterItemsByDate(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        // Nếu không chọn ngày → quay lại danh sách
        if (date == null) {
            return "redirect:/items/showview";
        }

        // Lọc theo ngày
        List<ItemWithAlertResponseDTO> items = itemService.getItemsContainingDate(date);

        model.addAttribute("items", items);
        model.addAttribute("selectedDate", date.toString());

        return "items";
    }

    //5 đảo trạng thái
    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Long id) {
        itemService.toggleItemStatus(id);

        // Load lại trang danh sách hiện tại
        return "redirect:/items/showview";
    }
}