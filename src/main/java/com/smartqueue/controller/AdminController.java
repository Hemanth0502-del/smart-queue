package com.smartqueue.controller;

import com.smartqueue.dto.CategoryForm;
import com.smartqueue.dto.CounterForm;
import com.smartqueue.entity.CounterStatus;
import com.smartqueue.repository.UserRepository;
import com.smartqueue.repository.CategoryRepository;
import com.smartqueue.repository.CounterRepository;
import com.smartqueue.service.AdminManagementService;
import com.smartqueue.service.DashboardService;
import com.smartqueue.service.ReportsService;
import com.smartqueue.service.TokenService;
import jakarta.validation.Valid;
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

    public AdminController(DashboardService dashboardService,
                           CategoryRepository categoryRepository,
                           CounterRepository counterRepository,
                           UserRepository userRepository,
                           TokenService tokenService,
                           AdminManagementService adminManagementService,
                           ReportsService reportsService) {
        this.dashboardService = dashboardService;
        this.categoryRepository = categoryRepository;
        this.counterRepository = counterRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.adminManagementService = adminManagementService;
        this.reportsService = reportsService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.getStats());
        model.addAttribute("recentTokens", tokenService.recentTokens());
        return "admin/dashboard";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        addCategoryModel(model, new CategoryForm(), null);
        return "admin/categories";
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategory(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return categoryRepository.findById(id)
                .map(category -> {
                    CategoryForm form = new CategoryForm();
                    form.setName(category.getName());
                    form.setDescription(category.getDescription());
                    form.setTokenPrefix(category.getTokenPrefix());
                    addCategoryModel(model, form, id);
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
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addCategoryModel(model, form, null);
            return "admin/categories";
        }

        try {
            adminManagementService.createCategory(form);
            redirectAttributes.addFlashAttribute("successMessage", "Category added successfully");
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("name", "category.invalid", ex.getMessage());
            addCategoryModel(model, form, null);
            return "admin/categories";
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @Valid @ModelAttribute("categoryForm") CategoryForm form,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addCategoryModel(model, form, id);
            return "admin/categories";
        }

        try {
            adminManagementService.updateCategory(id, form);
            redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully");
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("name", "category.invalid", ex.getMessage());
            addCategoryModel(model, form, id);
            return "admin/categories";
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/activate")
    public String activateCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminManagementService.setCategoryActive(id, true);
            redirectAttributes.addFlashAttribute("successMessage", "Category activated");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/deactivate")
    public String deactivateCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminManagementService.setCategoryActive(id, false);
            redirectAttributes.addFlashAttribute("successMessage", "Category deactivated");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/counters")
    public String counters(Model model) {
        addCounterModel(model, new CounterForm(), null);
        return "admin/counters";
    }

    @GetMapping("/counters/{id}/edit")
    public String editCounter(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return counterRepository.findById(id)
                .map(counter -> {
                    CounterForm form = new CounterForm();
                    form.setName(counter.getName());
                    form.setCode(counter.getCode());
                    form.setCategoryId(counter.getCategory().getId());
                    form.setStatus(counter.getStatus());
                    form.setAverageServiceTimeInMinutes(counter.getAverageServiceTimeInMinutes());
                    addCounterModel(model, form, id);
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
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addCounterModel(model, form, null);
            return "admin/counters";
        }

        try {
            adminManagementService.createCounter(form);
            redirectAttributes.addFlashAttribute("successMessage", "Counter added successfully");
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("code", "counter.invalid", ex.getMessage());
            addCounterModel(model, form, null);
            return "admin/counters";
        }
        return "redirect:/admin/counters";
    }

    @PostMapping("/counters/{id}")
    public String updateCounter(@PathVariable Long id,
                                @Valid @ModelAttribute("counterForm") CounterForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addCounterModel(model, form, id);
            return "admin/counters";
        }

        try {
            adminManagementService.updateCounter(id, form);
            tokenService.recalculateEstimatesForCounter(id);
            redirectAttributes.addFlashAttribute("successMessage", "Counter updated successfully");
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("code", "counter.invalid", ex.getMessage());
            addCounterModel(model, form, id);
            return "admin/counters";
        }
        return "redirect:/admin/counters";
    }

    @PostMapping("/counters/{id}/open")
    public String openCounter(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        updateCounterStatus(id, CounterStatus.OPEN, "Counter opened", redirectAttributes);
        return "redirect:/admin/counters";
    }

    @PostMapping("/counters/{id}/pause")
    public String pauseCounter(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        updateCounterStatus(id, CounterStatus.PAUSED, "Counter paused", redirectAttributes);
        return "redirect:/admin/counters";
    }

    @PostMapping("/counters/{id}/close")
    public String closeCounter(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        updateCounterStatus(id, CounterStatus.CLOSED, "Counter closed", redirectAttributes);
        return "redirect:/admin/counters";
    }

    private void updateCounterStatus(Long id,
                                     CounterStatus status,
                                     String successMessage,
                                     RedirectAttributes redirectAttributes) {
        try {
            adminManagementService.setCounterStatus(id, status);
            tokenService.recalculateEstimatesForCounter(id);
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
    }

    private void addCategoryModel(Model model, CategoryForm form, Long editingCategoryId) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("categoryForm", form);
        model.addAttribute("editingCategoryId", editingCategoryId);
    }

    private void addCounterModel(Model model, CounterForm form, Long editingCounterId) {
        model.addAttribute("counters", counterRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("statuses", CounterStatus.values());
        model.addAttribute("counterForm", form);
        model.addAttribute("editingCounterId", editingCounterId);
    }

    @GetMapping("/tokens")
    public String tokens(Model model) {
        model.addAttribute("tokens", tokenService.allTokens());
        return "admin/tokens";
    }

    @GetMapping("/reports")
    public String reports(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                          @RequestParam(required = false) Long categoryId,
                          Model model) {
        model.addAttribute("report", reportsService.dashboard(date, categoryId));
        return "admin/reports";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users";
    }
}
