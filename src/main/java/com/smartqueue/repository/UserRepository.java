package com.smartqueue.repository;

import com.smartqueue.entity.Role;
import com.smartqueue.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByCategoryIdAndRoleOrderByCreatedAtDesc(Long categoryId, Role role);
}
