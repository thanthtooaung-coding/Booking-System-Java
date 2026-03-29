package com.alvin.bookingsystem.job;

import com.alvin.bookingsystem.cache.CacheRegions;
import com.alvin.bookingsystem.cache.CrudResponseCache;
import com.alvin.bookingsystem.domain.model.UserPackage;
import com.alvin.bookingsystem.domain.repository.UserPackageRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@DisallowConcurrentExecution
public class ExpiredUserPackageJob implements Job {

    @Autowired
    private UserPackageRepository userPackageRepository;

    @Autowired(required = false)
    private CrudResponseCache crudResponseCache;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Value("${quartz.job.expired-package.batch-size:50}")
    private int batchSize;

    @Value("${quartz.job.expired-package.enabled:true}")
    private boolean jobEnabled;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (!jobEnabled) {
            log.info("ExpiredUserPackageJob is disabled. Skipping execution.");
            return;
        }

        log.info("Starting ExpiredUserPackageJob execution at {}", LocalDateTime.now());

        try {
            int cpuCores = Runtime.getRuntime().availableProcessors();
            int threadPoolSize = Math.max(2, cpuCores * 2);
            
            List<UserPackage> expiredPackages = userPackageRepository.findExpiredActivePackages(LocalDateTime.now());
            
            if (expiredPackages.isEmpty()) {
                log.info("No expired packages found. Job completed.");
                return;
            }

            log.info("Found {} expired packages to update. Using {} threads with batch size {}", 
                    expiredPackages.size(), threadPoolSize, batchSize);

            AtomicInteger processedCount = new AtomicInteger(0);
            AtomicInteger updatedCount = new AtomicInteger(0);

            try (ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize)) {
                List<List<UserPackage>> batches = partitionList(expiredPackages, batchSize);
                
                List<CompletableFuture<Void>> futures = batches.stream()
                        .map(batch -> CompletableFuture.runAsync(() -> {
                            try {
                                int batchUpdated = processBatch(batch);
                                updatedCount.addAndGet(batchUpdated);
                                processedCount.addAndGet(batch.size());
                                log.debug("Processed batch of {} packages, {} updated", batch.size(), batchUpdated);
                            } catch (Exception e) {
                                log.error("Error processing batch", e);
                            }
                        }, executorService))
                        .toList();

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                log.info("ExpiredUserPackageJob completed successfully. Processed: {}, Updated: {}", 
                        processedCount.get(), updatedCount.get());
            }

        } catch (Exception e) {
            log.error("Error executing ExpiredUserPackageJob", e);
            throw new JobExecutionException("Failed to execute ExpiredUserPackageJob", e);
        }
    }

    private int processBatch(List<UserPackage> batch) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        
        Integer result = transactionTemplate.execute(status -> {
            int updated = 0;
            try {
                for (UserPackage userPackage : batch) {
                    if (userPackage.getStatus() == UserPackage.Status.ACTIVE && 
                        userPackage.getExpiresAt().isBefore(LocalDateTime.now())) {
                        userPackage.setStatus(UserPackage.Status.EXPIRED);
                        userPackageRepository.save(userPackage);
                        if (crudResponseCache != null && crudResponseCache.isEnabled()) {
                            crudResponseCache.evict(CacheRegions.USER_PACKAGES, userPackage.getId());
                        }
                        updated++;
                    }
                }
                return updated;
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("Error processing batch, rolling back transaction", e);
                throw e;
            }
        });
        
        return result != null ? result : 0;
    }

    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return partitions;
    }
}
