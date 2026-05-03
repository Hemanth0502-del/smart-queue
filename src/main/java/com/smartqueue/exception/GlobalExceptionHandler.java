package com.smartqueue.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied() {
        return "redirect:/access-denied";
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            ConstraintViolationException.class,
            DataIntegrityViolationException.class,
            MethodArgumentNotValidException.class
    })
    public String handleKnownExceptions(Exception ex, HttpServletRequest request, Model model) {
        model.addAttribute("statusCode", 400);
        model.addAttribute("errorTitle", "Request could not be processed");
        model.addAttribute("errorMessage", userFriendlyMessage(ex));
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpectedException(Exception ex, HttpServletRequest request, Model model) {
        model.addAttribute("statusCode", 500);
        model.addAttribute("errorTitle", "Something went wrong");
        model.addAttribute("errorMessage", "Please try again or contact the system administrator.");
        model.addAttribute("path", request.getRequestURI());
        return "error";
    }

    private String userFriendlyMessage(Exception ex) {
        if (ex instanceof DataIntegrityViolationException) {
            return "This record could not be saved because it conflicts with existing data.";
        }
        if (ex instanceof MethodArgumentNotValidException) {
            return "Please check the form values and try again.";
        }
        return ex.getMessage() == null || ex.getMessage().isBlank()
                ? "Please check the request and try again."
                : ex.getMessage();
    }
}
