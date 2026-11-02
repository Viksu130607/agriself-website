package com.agriself.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Custom Error Controller.
 * Ensures stack traces and server internals are completely masked.
 */
@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = 500;
        String message = "An unexpected error occurred. Please try again or return home.";

        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                message = "The requested resource could not be found.";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                message = "Access to this page is strictly restricted.";
            } else if (statusCode == HttpStatus.TOO_MANY_REQUESTS.value()) {
                message = "Rate limit reached. Please wait a moment before trying again.";
            }
        }

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorMessage", message);
        return "error";
    }
}
