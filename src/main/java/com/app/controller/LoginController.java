package com.app.controller;

import com.app.session.UserSessionBean;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {

    @Autowired
    private UserSessionBean userSessionBean;

    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        Boolean showLogoutMessage = (Boolean) session.getAttribute("showLogoutMessage");
        if (Boolean.TRUE.equals(showLogoutMessage)) {
            model.addAttribute("logoutMessage", true);
            session.removeAttribute("showLogoutMessage");
        }
        return "login";
    }

    @PostMapping("/logout")
    public String logoutSuccess(HttpSession session) {
        userSessionBean.setLoggedIn(false);
        session.setAttribute("logoutMessage", true);
        return "redirect:/login";
    }

    @GetMapping("/logout-confirm")
    public String logoutConfirm() {
        return "logout-confirm";
    }
}
