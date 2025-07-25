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

    @GetMapping("/subjects")
    public String subjectListPage(Model model) {
        // Truyền dữ liệu ví dụ
        model.addAttribute("title", "Quản lý môn học");
        return "admin/subject"; // file: src/main/resources/templates/admin/subject.html
    }

    @GetMapping("/major")
    public String majorListPage(Model model) {
        // Truyền dữ liệu ví dụ
        model.addAttribute("title", "Quản lý ngành");
        return "admin/major"; // file: src/main/resources/templates/admin/subject.html
    }

    @GetMapping("/schools")
    public String schoolListPage(Model model) {
        // Truyền dữ liệu ví dụ
        model.addAttribute("title", "Quản lý trường");
        return "admin/school";
    }

}
