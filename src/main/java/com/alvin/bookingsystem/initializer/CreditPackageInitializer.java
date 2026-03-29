package com.alvin.bookingsystem.initializer;

import com.alvin.bookingsystem.domain.model.Country;
import com.alvin.bookingsystem.domain.model.CreditPackage;
import com.alvin.bookingsystem.domain.repository.CountryRepository;
import com.alvin.bookingsystem.domain.repository.CreditPackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class CreditPackageInitializer implements CommandLineRunner {

    private final CreditPackageRepository creditPackageRepository;
    private final CountryRepository countryRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (creditPackageRepository.count() > 0) {
            log.info("Credit packages already initialized. Skipping...");
            return;
        }

        log.info("Initializing credit packages...");

        Optional<Country> sgCountry = countryRepository.findByCode("SG");
        Optional<Country> mmCountry = countryRepository.findByCode("MM");

        if (sgCountry.isEmpty() || mmCountry.isEmpty()) {
            log.warn("Required countries not found. Please initialize countries first.");
            return;
        }

        List<CreditPackage> creditPackages = List.of(
                // Singapore packages
                CreditPackage.builder()
                        .name("Basic Package SG")
                        .country(sgCountry.get())
                        .credits(10)
                        .price(new BigDecimal("99.00"))
                        .validityDays(30)
                        .description("Basic package with 10 credits, valid for 30 days")
                        .active(true)
                        .build(),
                CreditPackage.builder()
                        .name("Premium Package SG")
                        .country(sgCountry.get())
                        .credits(30)
                        .price(new BigDecimal("249.00"))
                        .validityDays(60)
                        .description("Premium package with 30 credits, valid for 60 days")
                        .active(true)
                        .build(),
                CreditPackage.builder()
                        .name("VIP Package SG")
                        .country(sgCountry.get())
                        .credits(50)
                        .price(new BigDecimal("399.00"))
                        .validityDays(90)
                        .description("VIP package with 50 credits, valid for 90 days")
                        .active(true)
                        .build(),
                // Myanmar packages
                CreditPackage.builder()
                        .name("Basic Package MM")
                        .country(mmCountry.get())
                        .credits(10)
                        .price(new BigDecimal("50000.00"))
                        .validityDays(30)
                        .description("Basic package with 10 credits, valid for 30 days")
                        .active(true)
                        .build(),
                CreditPackage.builder()
                        .name("Premium Package MM")
                        .country(mmCountry.get())
                        .credits(30)
                        .price(new BigDecimal("120000.00"))
                        .validityDays(60)
                        .description("Premium package with 30 credits, valid for 60 days")
                        .active(true)
                        .build()
        );

        for (CreditPackage creditPackage : creditPackages) {
            if (creditPackageRepository.findByNameAndCountryId(creditPackage.getName(), creditPackage.getCountry().getId()).isEmpty()) {
                creditPackage.setCreatedById(0L);
                CreditPackage saved = creditPackageRepository.save(creditPackage);
                creditPackageRepository.updateCreatedById(saved.getId(), saved.getId());
                log.info("Initialized credit package: {} ({})", creditPackage.getName(), creditPackage.getCountry().getCode());
            }
        }

        log.info("Credit package initialization completed. Total credit packages: {}", creditPackageRepository.count());
    }
}
