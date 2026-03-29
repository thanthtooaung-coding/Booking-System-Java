package com.alvin.bookingsystem.initializer;

import com.alvin.bookingsystem.domain.model.Country;
import com.alvin.bookingsystem.domain.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class CountryInitializer implements CommandLineRunner {

    private final CountryRepository countryRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (countryRepository.count() > 0) {
            log.info("Countries already initialized. Skipping...");
            return;
        }

        log.info("Initializing countries...");

        List<Country> countries = List.of(
                Country.builder()
                        .code("SG")
                        .name("Singapore")
                        .active(true)
                        .build(),
                Country.builder()
                        .code("MM")
                        .name("Myanmar")
                        .active(true)
                        .build(),
                Country.builder()
                        .code("MY")
                        .name("Malaysia")
                        .active(true)
                        .build(),
                Country.builder()
                        .code("TH")
                        .name("Thailand")
                        .active(true)
                        .build(),
                Country.builder()
                        .code("ID")
                        .name("Indonesia")
                        .active(true)
                        .build()
        );

        for (Country country : countries) {
            if (countryRepository.findByCode(country.getCode()).isEmpty()) {
                country.setCreatedById(0L);
                Country saved = countryRepository.save(country);
                countryRepository.updateCreatedById(saved.getId(), saved.getId());
                log.info("Initialized country: {} ({})", country.getName(), country.getCode());
            }
        }

        log.info("Country initialization completed. Total countries: {}", countryRepository.count());
    }
}
