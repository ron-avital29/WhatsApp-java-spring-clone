package com.app.controller;

import com.app.session.UserSessionBean;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * LoginController handles login and logout operations.
 * It provides methods to display the login page, handle logout,
 * and confirm logout actions.
 */
@Controller
public class LoginController {

    /**
     * Session bean to manage user session data.
     */
    @Autowired
    private UserSessionBean userSessionBean;

    /**
     * Displays the login page.
     * If a logout message is present in the session, it adds an attribute to the model
     * to show a logout confirmation message.
     *
     * @param session the HTTP session
     * @param model   the model to add attributes for the view
     * @return the name of the login view
     */
    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        Boolean showLogoutMessage = (Boolean) session.getAttribute("showLogoutMessage");
        if (Boolean.TRUE.equals(showLogoutMessage)) {
            model.addAttribute("logoutMessage", true);
            session.removeAttribute("showLogoutMessage");
        }
        return "login";
    }

    /**
     * Handles the logout operation.
     * Sets the user session to logged out and adds a logout message to the session.
     *
     * @param session the HTTP session
     * @return a redirect to the login page
     */
    @PostMapping("/logout")
    public String logoutSuccess(HttpSession session) {
        userSessionBean.setLoggedIn(false);
        session.setAttribute("logoutMessage", true);
        return "redirect:/login";
    }

    /**
     * Displays the logout confirmation page.
     * This page is shown after a successful logout.
     *
     * @return the name of the logout confirmation view
     */
    @GetMapping("/logout-confirm")
    public String logoutConfirm() {
        return "logout-confirm";
    }
}
