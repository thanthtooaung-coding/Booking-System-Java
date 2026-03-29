package com.alvin.bookingsystem.initializer;

import com.alvin.bookingsystem.domain.model.ClassDefinition;
import com.alvin.bookingsystem.domain.model.Country;
import com.alvin.bookingsystem.domain.repository.ClassDefinitionRepository;
import com.alvin.bookingsystem.domain.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class ClassDefinitionInitializer implements CommandLineRunner {

    private final ClassDefinitionRepository classDefinitionRepository;
    private final CountryRepository countryRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (classDefinitionRepository.count() > 0) {
            log.info("Class definitions already initialized. Skipping...");
            return;
        }

        log.info("Initializing class definitions...");

        Optional<Country> sgCountry = countryRepository.findByCode("SG");
        Optional<Country> mmCountry = countryRepository.findByCode("MM");

        if (sgCountry.isEmpty() || mmCountry.isEmpty()) {
            log.warn("Required countries not found. Please initialize countries first.");
            return;
        }

        List<ClassDefinition> classDefinitions = List.of(
                // Singapore classes
                ClassDefinition.builder()
                        .name("1 Hour Yoga Class")
                        .country(sgCountry.get())
                        .requiredCredits(2)
                        .durationMinutes(60)
                        .description("A relaxing 1-hour yoga session focusing on flexibility and mindfulness")
                        .instructorName("Sarah Lee")
                        .active(true)
                        .build(),
                ClassDefinition.builder()
                        .name("45 Min Pilates Class")
                        .country(sgCountry.get())
                        .requiredCredits(1)
                        .durationMinutes(45)
                        .description("Core strengthening and body conditioning through Pilates")
                        .instructorName("James Tan")
                        .active(true)
                        .build(),
                ClassDefinition.builder()
                        .name("30 Min HIIT Class")
                        .country(sgCountry.get())
                        .requiredCredits(1)
                        .durationMinutes(30)
                        .description("High-intensity interval training for maximum calorie burn")
                        .instructorName("Michelle Wong")
                        .active(true)
                        .build(),
                ClassDefinition.builder()
                        .name("1 Hour Yoga Class")
                        .country(mmCountry.get())
                        .requiredCredits(2)
                        .durationMinutes(60)
                        .description("A relaxing 1-hour yoga session focusing on flexibility and mindfulness")
                        .instructorName("Aung Min")
                        .active(true)
                        .build(),
                ClassDefinition.builder()
                        .name("45 Min Zumba Class")
                        .country(mmCountry.get())
                        .requiredCredits(1)
                        .durationMinutes(45)
                        .description("Dance fitness class with Latin music")
                        .instructorName("Su Su")
                        .active(true)
                        .build()
        );

        for (ClassDefinition classDefinition : classDefinitions) {
            classDefinition.setCreatedById(0L);
            ClassDefinition saved = classDefinitionRepository.save(classDefinition);
            classDefinitionRepository.updateCreatedById(saved.getId(), saved.getId());
            log.info("Initialized class definition: {} ({})", classDefinition.getName(), classDefinition.getCountry().getCode());
        }

        log.info("Class definition initialization completed. Total class definitions: {}", classDefinitionRepository.count());
    }
}
