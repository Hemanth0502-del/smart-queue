package com.smartqueue.controller;

import com.smartqueue.service.LiveQueueService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final LiveQueueService liveQueueService;

    public HomeController(LiveQueueService liveQueueService) {
        this.liveQueueService = liveQueueService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/live")
    public String liveDashboard(@RequestParam(required = false) Long categoryId, Model model) {
        model.addAttribute("dashboard", liveQueueService.dashboard(categoryId));
        return "live-dashboard";
    }
}
