package com.smartqueue.config;

import com.smartqueue.entity.Category;
import com.smartqueue.entity.Role;
import com.smartqueue.entity.User;
import com.smartqueue.repository.CategoryRepository;
import com.smartqueue.repository.CounterRepository;
import com.smartqueue.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               CategoryRepository categoryRepository,
                               CounterRepository counterRepository,
                               PasswordEncoder passwordEncoder) {
        return new SeedDataRunner(userRepository, categoryRepository, counterRepository, passwordEncoder);
    }

    private record SeedUser(String fullName, String email, String rawPassword, Role role, String categoryName) {
    }

    private static final class SeedDataRunner implements CommandLineRunner {

        private final UserRepository userRepository;
        private final CategoryRepository categoryRepository;
        private final CounterRepository counterRepository;
        private final PasswordEncoder passwordEncoder;

        private SeedDataRunner(UserRepository userRepository,
                               CategoryRepository categoryRepository,
                               CounterRepository counterRepository,
                               PasswordEncoder passwordEncoder) {
            this.userRepository = userRepository;
            this.categoryRepository = categoryRepository;
            this.counterRepository = counterRepository;
            this.passwordEncoder = passwordEncoder;
        }

        @Override
        public void run(String... args) {
            Map<String, Category> categoriesByName = requiredCategories(categoryRepository);

            seedUser(userRepository, passwordEncoder,
                    "Demo User One", "user1@smartqueue.local", "user123", Role.USER, null);
            seedUser(userRepository, passwordEncoder,
                    "Demo User Two", "user2@smartqueue.local", "user123", Role.USER, null);

            List<SeedUser> staffSeeds = List.of(
                    new SeedUser("Hospital Staff", "staff.hospital@smartqueue.local", "staff123", Role.STAFF, "Hospital"),
                    new SeedUser("Bank Staff", "staff.bank@smartqueue.local", "staff123", Role.STAFF, "Bank"),
                    new SeedUser("College Staff", "staff.college@smartqueue.local", "staff123", Role.STAFF, "College"),
                    new SeedUser("Government Office Staff", "staff.gov@smartqueue.local", "staff123", Role.STAFF, "Government Office"),
                    new SeedUser("Restaurant Staff", "staff.restaurant@smartqueue.local", "staff123", Role.STAFF, "Restaurant"),
                    new SeedUser("Service Center Staff", "staff.service@smartqueue.local", "staff123", Role.STAFF, "Service Center")
            );

            List<SeedUser> adminSeeds = List.of(
                    new SeedUser("Hospital Admin", "admin.hospital@smartqueue.local", "admin123", Role.ADMIN, "Hospital"),
                    new SeedUser("Bank Admin", "admin.bank@smartqueue.local", "admin123", Role.ADMIN, "Bank"),
                    new SeedUser("College Admin", "admin.college@smartqueue.local", "admin123", Role.ADMIN, "College"),
                    new SeedUser("Government Office Admin", "admin.gov@smartqueue.local", "admin123", Role.ADMIN, "Government Office"),
                    new SeedUser("Restaurant Admin", "admin.restaurant@smartqueue.local", "admin123", Role.ADMIN, "Restaurant"),
                    new SeedUser("Service Center Admin", "admin.service@smartqueue.local", "admin123", Role.ADMIN, "Service Center")
            );

            staffSeeds.forEach(seed -> seedUser(
                    userRepository,
                    passwordEncoder,
                    seed.fullName(),
                    seed.email(),
                    seed.rawPassword(),
                    seed.role(),
                    categoriesByName.get(seed.categoryName())
            ));

            adminSeeds.forEach(seed -> seedUser(
                    userRepository,
                    passwordEncoder,
                    seed.fullName(),
                    seed.email(),
                    seed.rawPassword(),
                    seed.role(),
                    categoriesByName.get(seed.categoryName())
            ));

            Map<Long, User> staffByCategoryId = staffSeeds.stream()
                    .collect(Collectors.toMap(
                            seed -> categoriesByName.get(seed.categoryName()).getId(),
                            seed -> userRepository.findByEmail(seed.email())
                                    .orElseThrow(() -> new IllegalStateException("Seed staff was not created: " + seed.email()))
                    ));

            counterRepository.findAll().forEach(counter -> {
                User categoryStaff = staffByCategoryId.get(counter.getCategory().getId());
                if (categoryStaff != null
                        && (counter.getAssignedStaff() == null
                        || !counter.getAssignedStaff().getId().equals(categoryStaff.getId()))) {
                    counter.setAssignedStaff(categoryStaff);
                    counterRepository.save(counter);
                }
            });
        }

        private Map<String, Category> requiredCategories(CategoryRepository categoryRepository) {
            List<String> categoryNames = List.of(
                    "Hospital",
                    "Bank",
                    "College",
                    "Government Office",
                    "Restaurant",
                    "Service Center"
            );

            Map<String, Category> categoriesByName = categoryRepository.findAll().stream()
                    .collect(Collectors.toMap(Category::getName, Function.identity()));

            categoryNames.forEach(categoryName -> {
                if (!categoriesByName.containsKey(categoryName)) {
                    throw new IllegalStateException("Required seed category is missing: " + categoryName);
                }
            });
            return categoriesByName;
        }

        private User seedUser(UserRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              String fullName,
                              String email,
                              String rawPassword,
                              Role role,
                              Category category) {
            return userRepository.findByEmail(email)
                    .map(existing -> {
                        existing.setFullName(fullName);
                        existing.setRole(role);
                        existing.setCategory(role == Role.USER ? null : category);
                        existing.setActive(true);
                        if (existing.getPassword() == null || !existing.getPassword().startsWith("$2")) {
                            existing.setPassword(passwordEncoder.encode(rawPassword));
                        }
                        return userRepository.save(existing);
                    })
                    .orElseGet(() -> {
                        User user = new User();
                        user.setFullName(fullName);
                        user.setEmail(email);
                        user.setPassword(passwordEncoder.encode(rawPassword));
                        user.setRole(role);
                        user.setCategory(role == Role.USER ? null : category);
                        user.setActive(true);
                        return userRepository.save(user);
                    });
        }
    }
}
