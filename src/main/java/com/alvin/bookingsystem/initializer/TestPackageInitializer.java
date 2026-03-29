package com.alvin.bookingsystem.initializer;

import com.alvin.bookingsystem.domain.model.Country;
import com.alvin.bookingsystem.domain.model.CreditPackage;
import com.alvin.bookingsystem.domain.model.User;
import com.alvin.bookingsystem.domain.model.UserPackage;
import com.alvin.bookingsystem.domain.repository.CountryRepository;
import com.alvin.bookingsystem.domain.repository.CreditPackageRepository;
import com.alvin.bookingsystem.domain.repository.UserPackageRepository;
import com.alvin.bookingsystem.domain.repository.UserRepository;
import com.alvin.bookingsystem.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
@Order(5)
@RequiredArgsConstructor
public class TestPackageInitializer implements CommandLineRunner {

    private final CreditPackageRepository creditPackageRepository;
    private final CountryRepository countryRepository;
    private final UserPackageRepository userPackageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        Optional<Country> sgCountry = countryRepository.findByCode("SG");
        if (sgCountry.isEmpty()) {
            log.warn("Singapore country not found. Skipping test package creation.");
            return;
        }

        String testPackageName = "Test Package 5 Min SG";
        Optional<CreditPackage> existingPackage = creditPackageRepository.findByNameAndCountryId(testPackageName, sgCountry.get().getId());
        
        CreditPackage testPackage;
        if (existingPackage.isPresent()) {
            testPackage = existingPackage.get();
            log.info("Test package already exists: {} (ID: {})", testPackageName, testPackage.getId());
        } else {
            log.info("Creating test package with 5 minutes validity for scheduler testing...");

            testPackage = CreditPackage.builder()
                    .name(testPackageName)
                    .country(sgCountry.get())
                    .credits(5)
                    .price(new BigDecimal("9.99"))
                    .validityDays(1)
                    .description("Test package for scheduler testing - UserPackage expires in 5 minutes")
                    .active(true)
                    .build();

            testPackage.setCreatedById(0L);
            testPackage = creditPackageRepository.save(testPackage);
            creditPackageRepository.updateCreatedById(testPackage.getId(), testPackage.getId());
            log.info("Created test package: {} (ID: {})", testPackageName, testPackage.getId());
        }

        Optional<User> testUser = userRepository.findByEmail("test@bookingsystem.com");
        final User user;
        if (testUser.isEmpty()) {
            log.info("Creating test user for scheduler testing...");
            User newUser = User.builder()
                    .email("test@bookingsystem.com")
                    .password(PasswordUtil.encode("$2a$10$dummy"))
                    .firstName("Test")
                    .lastName("User")
                    .emailVerified(true)
                    .active(true)
                    .build();
            newUser.setCreatedById(0L);
            newUser = userRepository.save(newUser);
            userRepository.updateCreatedById(newUser.getId(), newUser.getId());
            user = newUser;
            log.info("Created test user: {} (ID: {})", user.getEmail(), user.getId());
        } else {
            user = testUser.get();
            log.info("Using existing test user: {} (ID: {})", user.getEmail(), user.getId());
        }

        final Long userId = user.getId();
        final Long testPackageId = testPackage.getId();
        boolean testUserPackageExists = userPackageRepository.findAll().stream()
                .anyMatch(up -> up.getUser().getId().equals(userId) && 
                              up.getCreditPackage().getId().equals(testPackageId));

        if (testUserPackageExists) {
            log.info("Test UserPackage already exists. Skipping creation.");
            log.info("Note: If you want to recreate the test package, delete the existing one first.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(5);

        UserPackage testUserPackage = UserPackage.builder()
                .user(user)
                .creditPackage(testPackage)
                .remainingCredits(testPackage.getCredits())
                .purchasedAt(now)
                .expiresAt(expiresAt)
                .status(UserPackage.Status.ACTIVE)
                .build();

        testUserPackage.setCreatedById(0L);
        testUserPackage = userPackageRepository.save(testUserPackage);
        userPackageRepository.updateCreatedById(testUserPackage.getId(), user.getId());

        log.info("Created test UserPackage (ID: {}) that expires at {} (in 5 minutes)", 
                testUserPackage.getId(), expiresAt);
        log.info("The Quartz scheduler will update this package status to EXPIRED after {}", expiresAt);
    }
}
