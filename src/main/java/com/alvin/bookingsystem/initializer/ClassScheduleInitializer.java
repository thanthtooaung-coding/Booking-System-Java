package com.alvin.bookingsystem.initializer;

import com.alvin.bookingsystem.domain.model.ClassDefinition;
import com.alvin.bookingsystem.domain.model.ClassSchedule;
import com.alvin.bookingsystem.domain.model.Country;
import com.alvin.bookingsystem.domain.repository.ClassDefinitionRepository;
import com.alvin.bookingsystem.domain.repository.ClassScheduleRepository;
import com.alvin.bookingsystem.domain.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Order(4)
@RequiredArgsConstructor
public class ClassScheduleInitializer implements CommandLineRunner {

    private final ClassScheduleRepository classScheduleRepository;
    private final ClassDefinitionRepository classDefinitionRepository;
    private final CountryRepository countryRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (classScheduleRepository.count() > 0) {
            log.info("Class schedules already initialized. Skipping...");
            return;
        }

        log.info("Initializing class schedules...");

        Optional<Country> sgCountry = countryRepository.findByCode("SG");
        Optional<Country> mmCountry = countryRepository.findByCode("MM");

        if (sgCountry.isEmpty() || mmCountry.isEmpty()) {
            log.warn("Required countries not found. Please initialize countries first.");
            return;
        }

        List<ClassDefinition> sgClasses = classDefinitionRepository.findAll().stream()
                .filter(cd -> "SG".equals(cd.getCountry().getCode()))
                .toList();

        List<ClassDefinition> mmClasses = classDefinitionRepository.findAll().stream()
                .filter(cd -> "MM".equals(cd.getCountry().getCode()))
                .toList();

        if (sgClasses.isEmpty() || mmClasses.isEmpty()) {
            log.warn("Class definitions not found. Please initialize class definitions first.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        LocalDateTime nextWeek = now.plusDays(7);

        List<ClassSchedule> classSchedules = List.of(
                // Singapore schedules - tomorrow
                ClassSchedule.builder()
                        .classDefinition(sgClasses.get(0)) // 1 Hour Yoga Class
                        .country(sgCountry.get())
                        .classDateTime(tomorrow.withHour(9).withMinute(0).withSecond(0).withNano(0))
                        .maxSlots(20)
                        .bookedSlots(0)
                        .status(ClassSchedule.Status.SCHEDULED)
                        .build(),
                ClassSchedule.builder()
                        .classDefinition(sgClasses.get(1)) // 45 Min Pilates Class
                        .country(sgCountry.get())
                        .classDateTime(tomorrow.withHour(14).withMinute(0).withSecond(0).withNano(0))
                        .maxSlots(15)
                        .bookedSlots(0)
                        .status(ClassSchedule.Status.SCHEDULED)
                        .build(),
                ClassSchedule.builder()
                        .classDefinition(sgClasses.get(2)) // 30 Min HIIT Class
                        .country(sgCountry.get())
                        .classDateTime(tomorrow.withHour(18).withMinute(0).withSecond(0).withNano(0))
                        .maxSlots(25)
                        .bookedSlots(0)
                        .status(ClassSchedule.Status.SCHEDULED)
                        .build(),
                // Singapore schedules - next week
                ClassSchedule.builder()
                        .classDefinition(sgClasses.get(0)) // 1 Hour Yoga Class
                        .country(sgCountry.get())
                        .classDateTime(nextWeek.withHour(9).withMinute(0).withSecond(0).withNano(0))
                        .maxSlots(20)
                        .bookedSlots(0)
                        .status(ClassSchedule.Status.SCHEDULED)
                        .build(),
                // Myanmar schedules - tomorrow
                ClassSchedule.builder()
                        .classDefinition(mmClasses.get(0)) // 1 Hour Yoga Class
                        .country(mmCountry.get())
                        .classDateTime(tomorrow.withHour(10).withMinute(0).withSecond(0).withNano(0))
                        .maxSlots(20)
                        .bookedSlots(0)
                        .status(ClassSchedule.Status.SCHEDULED)
                        .build(),
                ClassSchedule.builder()
                        .classDefinition(mmClasses.get(1)) // 45 Min Zumba Class
                        .country(mmCountry.get())
                        .classDateTime(tomorrow.withHour(17).withMinute(0).withSecond(0).withNano(0))
                        .maxSlots(30)
                        .bookedSlots(0)
                        .status(ClassSchedule.Status.SCHEDULED)
                        .build()
        );

        for (ClassSchedule classSchedule : classSchedules) {
            classSchedule.setCreatedById(0L); // System user
            ClassSchedule saved = classScheduleRepository.save(classSchedule);
            classScheduleRepository.updateCreatedById(saved.getId(), saved.getId());
            log.info("Initialized class schedule: {} at {} ({})",
                    classSchedule.getClassDefinition().getName(),
                    classSchedule.getClassDateTime(),
                    classSchedule.getCountry().getCode());
        }

        log.info("Class schedule initialization completed. Total class schedules: {}", classScheduleRepository.count());
    }
}
