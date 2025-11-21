package com.example._t1020159.viewcontroller;

import com.example._t1020159.dto.request.ItemWithAlertRequestDTO;
import com.example._t1020159.dto.response.CategoriesResponseDTO;
import com.example._t1020159.dto.response.ItemResponseDTO;
import com.example._t1020159.service.CategoriesService;
import com.example._t1020159.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/items")
public class ItemViewController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private CategoriesService categoriesService;

    // 1. Phương thức hiển thị Form tạo Item (Đã thêm logic truyền Category)
    @GetMapping("/new")
    public String showNewItemForm(Model model) {
        model.addAttribute("item", new ItemWithAlertRequestDTO());

        // Lấy danh sách Category DTO và truyền vào Model
        List<CategoriesResponseDTO> categories = categoriesService.getAllCategories();
        model.addAttribute("categories", categories);

        return "item-form";
    }

    // 2. Phương thức xử lý tạo Item
    @PostMapping("/create")
    public String createItem(@ModelAttribute("item") ItemWithAlertRequestDTO formDto) {
        // Logíc tạo Item (chứa logic lưu vào DB)
        itemService.createItem(formDto);

        // Điều hướng sau khi tạo thành công.
        // Bây giờ nó sẽ chuyển hướng đến mapping /showview ở dưới.
        return "redirect:/items/showview";
    }
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public String deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);

        // Chuyển hướng trình duyệt về trang danh sách
        return "redirect:/items/showview";
    }

    // 3. Phương thức Xem danh sách Item (Khắc phục lỗi Type Mismatch)
    // Đường dẫn cố định "/showview" được ưu tiên hơn các đường dẫn Path Variable
    @GetMapping("/showview")
    public String showAllItems(Model model) {
        // Lấy tất cả item từ DB
        List<ItemResponseDTO> items = itemService.getAllItems(null, null);

        // Thêm vào model để Thymeleaf hiển thị
        model.addAttribute("items", items);

        return "items"; // tên template hiển thị danh sách
    }


}