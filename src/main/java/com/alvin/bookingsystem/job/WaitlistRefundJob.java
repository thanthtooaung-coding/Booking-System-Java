package com.alvin.bookingsystem.job;

import com.alvin.bookingsystem.cache.CacheRegions;
import com.alvin.bookingsystem.cache.CrudResponseCache;
import com.alvin.bookingsystem.domain.model.Waitlist;
import com.alvin.bookingsystem.domain.repository.UserPackageRepository;
import com.alvin.bookingsystem.domain.repository.WaitlistRepository;
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
public class WaitlistRefundJob implements Job {

    @Autowired
    private WaitlistRepository waitlistRepository;

    @Autowired
    private UserPackageRepository userPackageRepository;

    @Autowired(required = false)
    private CrudResponseCache crudResponseCache;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Value("${quartz.job.waitlist-refund.batch-size:50}")
    private int batchSize;

    @Value("${quartz.job.waitlist-refund.enabled:true}")
    private boolean jobEnabled;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (!jobEnabled) {
            log.info("WaitlistRefundJob is disabled. Skipping execution.");
            return;
        }

        log.info("Starting WaitlistRefundJob execution at {}", LocalDateTime.now());

        try {
            int cpuCores = Runtime.getRuntime().availableProcessors();
            int threadPoolSize = Math.max(2, cpuCores * 2);
            
            List<Waitlist> waitlistsToRefund = waitlistRepository.findWaitlistsToRefund(LocalDateTime.now());
            
            if (waitlistsToRefund.isEmpty()) {
                log.info("No waitlists to refund. Job completed.");
                return;
            }

            log.info("Found {} waitlist entries to refund. Using {} threads with batch size {}", 
                    waitlistsToRefund.size(), threadPoolSize, batchSize);

            AtomicInteger processedCount = new AtomicInteger(0);
            AtomicInteger refundedCount = new AtomicInteger(0);

            try (ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize)) {
                List<List<Waitlist>> batches = partitionList(waitlistsToRefund, batchSize);
                
                List<CompletableFuture<Void>> futures = batches.stream()
                        .map(batch -> CompletableFuture.runAsync(() -> {
                            try {
                                int batchRefunded = processBatch(batch);
                                refundedCount.addAndGet(batchRefunded);
                                processedCount.addAndGet(batch.size());
                                log.debug("Processed batch of {} waitlists, {} refunded", batch.size(), batchRefunded);
                            } catch (Exception e) {
                                log.error("Error processing batch", e);
                            }
                        }, executorService))
                        .toList();

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                log.info("WaitlistRefundJob completed successfully. Processed: {}, Refunded: {}", 
                        processedCount.get(), refundedCount.get());
            }

        } catch (Exception e) {
            log.error("Error executing WaitlistRefundJob", e);
            throw new JobExecutionException("Failed to execute WaitlistRefundJob", e);
        }
    }

    private int processBatch(List<Waitlist> batch) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        
        Integer result = transactionTemplate.execute(status -> {
            int refunded = 0;
            try {
                for (Waitlist waitlist : batch) {
                    if (waitlist.getStatus() == Waitlist.Status.WAITING) {
                        var userPackage = waitlist.getUserPackage();
                        userPackage.setRemainingCredits(
                                userPackage.getRemainingCredits() + waitlist.getCreditsReserved()
                        );
                        userPackageRepository.save(userPackage);

                        waitlist.setStatus(Waitlist.Status.REFUNDED);
                        waitlist.setRefundedAt(LocalDateTime.now());
                        waitlistRepository.save(waitlist);

                        if (crudResponseCache != null && crudResponseCache.isEnabled()) {
                            crudResponseCache.evict(CacheRegions.USER_PACKAGES, userPackage.getId());
                            crudResponseCache.evict(CacheRegions.WAITLISTS, waitlist.getId());
                        }

                        refunded++;
                    }
                }
                return refunded;
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
