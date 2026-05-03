package com.smartqueue.controller;

import com.smartqueue.dto.TokenRequest;
import com.smartqueue.entity.Category;
import com.smartqueue.entity.PriorityType;
import com.smartqueue.entity.Role;
import com.smartqueue.entity.User;
import com.smartqueue.service.AccessControlService;
import com.smartqueue.service.TokenService;
import com.smartqueue.repository.CategoryRepository;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class UserController {

    private final CategoryRepository categoryRepository;
    private final TokenService tokenService;
    private final AccessControlService accessControlService;

    public UserController(CategoryRepository categoryRepository,
                          TokenService tokenService,
                          AccessControlService accessControlService) {
        this.categoryRepository = categoryRepository;
        this.tokenService = tokenService;
        this.accessControlService = accessControlService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Long categoryId,
                            Model model,
                            Principal principal) {
        String roleRedirect = redirectForNonUser(principal.getName());
        if (roleRedirect != null) {
            return roleRedirect;
        }
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setCategoryId(categoryId);
        addDashboardModel(model, principal.getName(), tokenRequest, categoryId);
        return "user/dashboard";
    }

    @PostMapping("/tokens")
    public String generateToken(@Valid @ModelAttribute("tokenRequest") TokenRequest request,
                                BindingResult bindingResult,
                                Model model,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addDashboardModel(model, principal.getName(), request, request.getCategoryId());
            return "user/dashboard";
        }

        try {
            redirectAttributes.addFlashAttribute("tokenNumber",
                    tokenService.generateToken(principal.getName(), request).getTokenNumber());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            addDashboardModel(model, principal.getName(), request, request.getCategoryId());
            return "user/dashboard";
        }
        return "redirect:/user/my-token";
    }

    @GetMapping("/tokens/status")
    public String tokenStatus(Model model, Principal principal) {
        model.addAttribute("tokens", tokenService.tokensForUser(principal.getName()));
        return "user/token-status";
    }

    @PostMapping("/tokens/{tokenId}/quit")
    public String quitQueue(@PathVariable Long tokenId,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        try {
            tokenService.quitQueue(tokenId, principal.getName());
            redirectAttributes.addFlashAttribute("message", "You have left the queue");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/user/dashboard";
    }

    @GetMapping("/my-token")
    public String myToken(Model model, Principal principal) {
        model.addAttribute("currentToken", tokenService.currentActiveToken(principal.getName()).orElse(null));
        model.addAttribute("tokens", tokenService.tokensForUser(principal.getName()));
        return "user/my-token";
    }

    private void addDashboardModel(Model model, String email, TokenRequest tokenRequest, Long categoryId) {
        model.addAttribute("tokenRequest", tokenRequest);
        model.addAttribute("categories", categoryRepository.findByActiveTrueOrderByNameAsc());
        model.addAttribute("priorityTypes", PriorityType.values());
        model.addAttribute("availableCounters", tokenService.availableCountersForCategory(categoryId));
        model.addAttribute("currentToken", tokenService.currentActiveToken(email).orElse(null));
        model.addAttribute("tokens", tokenService.tokensForUser(email));
        model.addAttribute("selectedCategory", selectedCategory(categoryId));
    }

    private Category selectedCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .filter(Category::isActive)
                .orElse(null);
    }

    private String redirectForNonUser(String email) {
        User user = accessControlService.currentUser(email);
        if (user.getRole() == Role.STAFF) {
            return "redirect:/staff/dashboard";
        }
        if (user.getRole() == Role.ADMIN) {
            return "redirect:/admin/dashboard";
        }
        return null;
    }
}
