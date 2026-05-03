package com.smartqueue.config;



@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(UserRepository userRepository,
                               CounterRepository counterRepository,
                               PasswordEncoder passwordEncoder,
                               @Value("${app.seed.admin.email}") String adminEmail,
                               @Value("${app.seed.admin.password}") String adminPassword,
                               @Value("${app.seed.staff.email}") String staffEmail,
                               @Value("${app.seed.staff.password}") String staffPassword) {
        return args -> {
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setFullName("System Administrator");
                admin.setPhone("0000000000");
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
            }

            User staff = userRepository.findByEmail(staffEmail).orElseGet(() -> {
                User newStaff = new User();
                newStaff.setEmail(staffEmail);
                newStaff.setPassword(passwordEncoder.encode(staffPassword));
                newStaff.setFullName("Queue Staff");
                newStaff.setPhone("1111111111");
                newStaff.setRole(Role.STAFF);
                return userRepository.save(newStaff);
            });

            counterRepository.findByCode("HOS-C01").ifPresent(counter -> {
                if (counter.getAssignedStaff() == null) {
                    counter.setAssignedStaff(staff);
                    counterRepository.save(counter);
                }
            });
            counterRepository.findByCode("BNK-C01").ifPresent(counter -> {
                if (counter.getAssignedStaff() == null) {
                    counter.setAssignedStaff(staff);
                    counterRepository.save(counter);
                }
            });
        };
    }
}
