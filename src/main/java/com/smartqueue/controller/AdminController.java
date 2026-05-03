package com.smartqueue.controller;

import com.smartqueue.dto.CategoryForm;
import com.smartqueue.dto.CounterForm;
import com.smartqueue.entity.Category;
import com.smartqueue.entity.CounterStatus;
import com.smartqueue.entity.Role;
import com.smartqueue.repository.UserRepository;
import com.smartqueue.repository.CategoryRepository;
import com.smartqueue.repository.CounterRepository;
import com.smartqueue.service.AccessControlService;
import com.smartqueue.service.AdminManagementService;
import com.smartqueue.service.DashboardService;
import com.smartqueue.service.ReportsService;
import com.smartqueue.service.TokenService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final DashboardService dashboardService;
    private final CategoryRepository categoryRepository;
    private final CounterRepository counterRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final AdminManagementService adminManagementService;
    private final ReportsService reportsService;
    private final AccessControlService accessControlService;

    public AdminController(DashboardService dashboardService,
                           CategoryRepository categoryRepository,
                           CounterRepository counterRepository,
                           UserRepository userRepository,
                           TokenService tokenService,
                           AdminManagementService adminManagementService,
                           ReportsService reportsService,
                           AccessControlService accessControlService) {
        this.dashboardService = dashboardService;
        this.categoryRepository = categoryRepository;
        this.counterRepository = counterRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.adminManagementService = adminManagementService;
        this.reportsService = reportsService;
        this.accessControlService = accessControlService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("stats", dashboardService.getStatsForAssignedCategory(principal.getName()));
        model.addAttribute("recentTokens", tokenService.recentTokensForAssignedCategory(principal.getName()));
        return "admin/dashboard";
    }

    @GetMapping("/categories")
    public String categories(Model model, Principal principal) {
        Category assignedCategory = accessControlService.assignedCategory(principal.getName());
        CategoryForm form = new CategoryForm();
        form.setName(assignedCategory.getName());
        form.setDescription(assignedCategory.getDescription());
        form.setTokenPrefix(assignedCategory.getTokenPrefix());
        addCategoryModel(model, form, assignedCategory.getId(), principal.getName());
        return "admin/categories";
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategory(@PathVariable Long id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        accessControlService.requireAssignedCategory(principal.getName(), id);
        return categoryRepository.findById(id)
                .map(category -> {
                    CategoryForm form = new CategoryForm();
                    form.setName(category.getName());
                    form.setDescription(category.getDescription());
                    form.setTokenPrefix(category.getTokenPrefix());
                    addCategoryModel(model, form, id, principal.getName());
                    return "admin/categories";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Category not found");
                    return "redirect:/admin/categories";
                });
    }

    @PostMapping("/categories")
    public String createCategory(@Valid @ModelAttribute("categoryForm") CategoryForm form,
                                 BindingResult bindingResult,
                                 Model model,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        accessControlService.assignedCategory(principal.getName());
        return "redirect:/access-denied";
    }

    @PostMapping("/categories/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @Valid @ModelAttribute("categoryForm") CategoryForm form,
                                 BindingResult bindingResult,
                                 Model model,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addCategoryModel(model, form, id, principal.getName());
            return "admin/categories";
        }

        try {
            adminManagementService.updateCategoryForUser(id, form, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully");
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("name", "category.invalid", ex.getMessage());
            addCategoryModel(model, form, id, principal.getName());
            return "admin/categories";
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/activate")
    public String activateCategory(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            adminManagementService.setCategoryActiveForUser(id, true, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Category activated");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/deactivate")
    public String deactivateCategory(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            adminManagementService.setCategoryActiveForUser(id, false, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Category deactivated");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/counters")
    public String counters(Model model, Principal principal) {
        Long assignedCategoryId = accessControlService.assignedCategoryId(principal.getName());
        CounterForm form = new CounterForm();
        form.setCategoryId(assignedCategoryId);
        addCounterModel(model, form, null, principal.getName());
        return "admin/counters";
    }

    @GetMapping("/counters/{id}/edit")
    public String editCounter(@PathVariable Long id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        return counterRepository.findById(id)
                .map(counter -> {
                    accessControlService.requireAssignedCategory(principal.getName(), counter.getCategory().getId());
                    CounterForm form = new CounterForm();
                    form.setName(counter.getName());
                    form.setCode(counter.getCode());
                    form.setCategoryId(counter.getCategory().getId());
                    form.setStatus(counter.getStatus());
                    form.setAverageServiceTimeInMinutes(counter.getAverageServiceTimeInMinutes());
                    addCounterModel(model, form, id, principal.getName());
                    return "admin/counters";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Counter not found");
                    return "redirect:/admin/counters";
                });
    }

    @PostMapping("/counters")
    public String createCounter(@Valid @ModelAttribute("counterForm") CounterForm form,
                                BindingResult bindingResult,
                                Model model,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        Long assignedCategoryId = accessControlService.assignedCategoryId(principal.getName());
        form.setCategoryId(assignedCategoryId);
        if (bindingResult.hasErrors()) {
            addCounterModel(model, form, null, principal.getName());
            return "admin/counters";
        }

        try {
            adminManagementService.createCounterForUser(form, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Counter added successfully");
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("code", "counter.invalid", ex.getMessage());
            addCounterModel(model, form, null, principal.getName());
            return "admin/counters";
        }
        return "redirect:/admin/counters";
    }

    @PostMapping("/counters/{id}")
    public String updateCounter(@PathVariable Long id,
                                @Valid @ModelAttribute("counterForm") CounterForm form,
                                BindingResult bindingResult,
                                Model model,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        Long assignedCategoryId = accessControlService.assignedCategoryId(principal.getName());
        form.setCategoryId(assignedCategoryId);
        if (bindingResult.hasErrors()) {
            addCounterModel(model, form, id, principal.getName());
            return "admin/counters";
        }

        try {
            adminManagementService.updateCounterForUser(id, form, principal.getName());
            tokenService.recalculateEstimatesForCounterInScope(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Counter updated successfully");
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("code", "counter.invalid", ex.getMessage());
            addCounterModel(model, form, id, principal.getName());
            return "admin/counters";
        }
        return "redirect:/admin/counters";
    }

    @PostMapping("/counters/{id}/open")
    public String openCounter(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        updateCounterStatus(id, CounterStatus.OPEN, "Counter opened", principal, redirectAttributes);
        return "redirect:/admin/counters";
    }

    @PostMapping("/counters/{id}/pause")
    public String pauseCounter(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        updateCounterStatus(id, CounterStatus.PAUSED, "Counter paused", principal, redirectAttributes);
        return "redirect:/admin/counters";
    }

    @PostMapping("/counters/{id}/close")
    public String closeCounter(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        updateCounterStatus(id, CounterStatus.CLOSED, "Counter closed", principal, redirectAttributes);
        return "redirect:/admin/counters";
    }

    private void updateCounterStatus(Long id,
                                     CounterStatus status,
                                     String successMessage,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        try {
            adminManagementService.setCounterStatusForUser(id, status, principal.getName());
            tokenService.recalculateEstimatesForCounterInScope(id, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
    }

    private void addCategoryModel(Model model, CategoryForm form, Long editingCategoryId, String email) {
        model.addAttribute("categories", adminManagementService.categoriesForUser(email));
        model.addAttribute("categoryForm", form);
        model.addAttribute("editingCategoryId", editingCategoryId);
    }

    private void addCounterModel(Model model, CounterForm form, Long editingCounterId, String email) {
        model.addAttribute("counters", adminManagementService.countersForUser(email));
        model.addAttribute("categories", adminManagementService.categoriesForUser(email));
        model.addAttribute("statuses", CounterStatus.values());
        model.addAttribute("counterForm", form);
        model.addAttribute("editingCounterId", editingCounterId);
    }

    @GetMapping("/tokens")
    public String tokens(Model model, Principal principal) {
        model.addAttribute("tokens", tokenService.allTokensForAssignedCategory(principal.getName()));
        return "admin/tokens";
    }

    @GetMapping("/reports")
    public String reports(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                          @RequestParam(required = false) Long categoryId,
                          Model model,
                          Principal principal) {
        model.addAttribute("report", reportsService.dashboardForAssignedCategory(
                date,
                categoryId,
                principal.getName()
        ));
        return "admin/reports";
    }

    @GetMapping("/users")
    public String users(Model model, Principal principal) {
        model.addAttribute("users", userRepository.findByCategoryIdAndRoleOrderByCreatedAtDesc(
                accessControlService.assignedCategoryId(principal.getName()),
                Role.STAFF
        ));
        return "admin/users";
    }
}
