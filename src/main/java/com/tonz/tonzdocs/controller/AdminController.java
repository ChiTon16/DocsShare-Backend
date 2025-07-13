package com.tonz.tonzdocs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Truyền dữ liệu ví dụ
        model.addAttribute("title", "Bảng điều khiển quản trị");
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userListPage(Model model) {
        // Không trùng vì đây trả về giao diện HTML
        return "admin/userList"; // file: src/main/resources/templates/admin/user-list.html
    }

}
