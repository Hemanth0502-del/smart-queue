package com.smartqueue.service;

import com.smartqueue.entity.Category;
import com.smartqueue.entity.Role;
import com.smartqueue.entity.User;
import com.smartqueue.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccessControlService {

    private final UserRepository userRepository;

    public AccessControlService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User currentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Logged-in user could not be found"));
    }

    @Transactional(readOnly = true)
    public Category assignedCategory(String email) {
        return getAssignedCategory(currentUser(email));
    }

    public Category getAssignedCategory(User user) {
        if ((user.getRole() == Role.ADMIN || user.getRole() == Role.STAFF) && user.getCategory() == null) {
            throw new AccessDeniedException("Your account is not assigned to a category");
        }
        Category category = user.getCategory();
        if (category != null) {
            category.getName();
        }
        return category;
    }

    public boolean canAccessCategory(User user, Long categoryId) {
        if (user == null || categoryId == null) {
            return false;
        }
        if (user.getRole() == Role.USER) {
            return true;
        }
        Category category = getAssignedCategory(user);
        return category != null && category.getId().equals(categoryId);
    }

    @Transactional(readOnly = true)
    public Long assignedCategoryId(String email) {
        return assignedCategory(email).getId();
    }

    @Transactional(readOnly = true)
    public void requireAssignedCategory(String email, Long categoryId) {
        if (!canAccessCategory(currentUser(email), categoryId)) {
            throw new AccessDeniedException("You are not authorized to access this category");
        }
    }

    public void requireCategoryAccess(User user, Long categoryId) {
        if (!canAccessCategory(user, categoryId)) {
            throw new AccessDeniedException("You are not authorized to access this category");
        }
    }
}
