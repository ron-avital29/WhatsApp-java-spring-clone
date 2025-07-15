package com.app.controller;

import jakarta.servlet.RequestDispatcher;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller to handle errors and display an error page.
 * This controller captures errors and displays a generic error page with the status code.
 */
@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    /**
     * Handles errors by capturing the status code and returning an error view.
     *
     * @param request the HTTP request containing error information
     * @param model   the model to add attributes for the view
     * @return the name of the error view
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        model.addAttribute("statusCode", status);
        return "error";
    }
}
