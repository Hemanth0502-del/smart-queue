package com.smartqueue.controller;

import com.smartqueue.entity.Category;
import com.smartqueue.entity.User;
import com.smartqueue.service.AccessControlService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class NavigationModelAdvice {

    private final AccessControlService accessControlService;

    public NavigationModelAdvice(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    @ModelAttribute("currentUser")
    public User currentUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        return accessControlService.currentUser(principal.getName());
    }

    @ModelAttribute("assignedCategory")
    public Category assignedCategory(Principal principal) {
        if (principal == null) {
            return null;
        }
        User user = accessControlService.currentUser(principal.getName());
        return user.getCategory();
    }
}
