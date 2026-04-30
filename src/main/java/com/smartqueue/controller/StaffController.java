package com.smartqueue.controller;

import com.smartqueue.service.TokenService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff")
public class StaffController {

    private final TokenService tokenService;

    public StaffController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("counterStatuses", tokenService.counterQueueStatuses(principal.getName()));
        model.addAttribute("categoryStatuses", tokenService.categoryQueueStatuses());
        model.addAttribute("waitingTokens", tokenService.waitingTokens());
        model.addAttribute("recentTokens", tokenService.recentTokens());
        return "staff/dashboard";
    }

    @PostMapping("/counters/{counterId}/call-next")
    public String callNext(@PathVariable Long counterId, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            redirectAttributes.addFlashAttribute("message",
                    "Called token " + tokenService.callNextToken(counterId, principal.getName()).getTokenNumber());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }
        return "redirect:/staff/dashboard";
    }

    @PostMapping("/counters/{counterId}/recall")
    public String recall(@PathVariable Long counterId, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            redirectAttributes.addFlashAttribute("message",
                    "Recalled token " + tokenService.recallCurrentToken(counterId, principal.getName()).getTokenNumber());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }
        return "redirect:/staff/dashboard";
    }

    @PostMapping("/tokens/{tokenId}/skip")
    public String skipToken(@PathVariable Long tokenId, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            tokenService.skipToken(tokenId, principal.getName());
            redirectAttributes.addFlashAttribute("message", "Token skipped");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }
        return "redirect:/staff/dashboard";
    }

    @PostMapping("/tokens/{tokenId}/complete")
    public String completeToken(@PathVariable Long tokenId, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            tokenService.completeToken(tokenId, principal.getName());
            redirectAttributes.addFlashAttribute("message", "Token completed");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }
        return "redirect:/staff/dashboard";
    }
}
