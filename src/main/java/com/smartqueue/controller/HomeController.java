package com.smartqueue.controller;

import com.smartqueue.entity.Role;
import com.smartqueue.entity.User;
import com.smartqueue.service.AccessControlService;
import com.smartqueue.service.LiveQueueService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final LiveQueueService liveQueueService;
    private final AccessControlService accessControlService;

    public HomeController(LiveQueueService liveQueueService, AccessControlService accessControlService) {
        this.liveQueueService = liveQueueService;
        this.accessControlService = accessControlService;
    }

    @GetMapping("/")
    public String home(Principal principal) {
        if (principal != null) {
            return roleDashboardRedirect(principal);
        }
        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal) {
        return roleDashboardRedirect(principal);
    }

    @GetMapping("/live")
    public String liveDashboard(@RequestParam(required = false) Long categoryId, Model model, Principal principal) {
        if (principal != null) {
            User user = accessControlService.currentUser(principal.getName());
            if (user.getRole() == Role.ADMIN || user.getRole() == Role.STAFF) {
                if (categoryId != null) {
                    accessControlService.requireAssignedCategory(principal.getName(), categoryId);
                }
                model.addAttribute("dashboard", liveQueueService.dashboardForAssignedCategory(principal.getName()));
                return "live-dashboard";
            }
        }
        model.addAttribute("dashboard", liveQueueService.dashboard(categoryId));
        return "live-dashboard";
    }

    private String roleDashboardRedirect(Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        User user = accessControlService.currentUser(principal.getName());
        if (user.getRole() == Role.ADMIN) {
            return "redirect:/admin/dashboard";
        }
        if (user.getRole() == Role.STAFF) {
            return "redirect:/staff/dashboard";
        }
        return "redirect:/user/dashboard";
    }
}
