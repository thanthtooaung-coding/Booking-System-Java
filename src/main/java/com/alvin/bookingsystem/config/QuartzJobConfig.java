package com.alvin.bookingsystem.config;

import com.alvin.bookingsystem.job.ExpiredUserPackageJob;
import com.alvin.bookingsystem.job.WaitlistRefundJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class QuartzJobConfig implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${quartz.job.expired-package.cron:0 0 * * * ?}")
    private String expiredPackageCronExpression;

    @Value("${quartz.job.expired-package.enabled:true}")
    private boolean expiredPackageJobEnabled;

    @Value("${quartz.job.waitlist-refund.cron:0 */15 * * * ?}")
    private String waitlistRefundCronExpression;

    @Value("${quartz.job.waitlist-refund.enabled:true}")
    private boolean waitlistRefundJobEnabled;

    @Bean
    public JobDetail expiredUserPackageJobDetail() {
        return JobBuilder.newJob(ExpiredUserPackageJob.class)
                .withIdentity("expiredUserPackageJob", "userPackageJobs")
                .withDescription("Job to update expired user packages status to EXPIRED")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger expiredUserPackageJobTrigger(JobDetail expiredUserPackageJobDetail) {
        if (!expiredPackageJobEnabled) {
            return null;
        }
        
        return TriggerBuilder.newTrigger()
                .forJob(expiredUserPackageJobDetail)
                .withIdentity("expiredUserPackageTrigger", "userPackageJobs")
                .withDescription("Trigger for expired user package job - runs every hour")
                .withSchedule(CronScheduleBuilder.cronSchedule(expiredPackageCronExpression)
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }

    @Bean
    public JobDetail waitlistRefundJobDetail() {
        return JobBuilder.newJob(WaitlistRefundJob.class)
                .withIdentity("waitlistRefundJob", "waitlistJobs")
                .withDescription("Job to refund waitlist credits after class ends")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger waitlistRefundJobTrigger(JobDetail waitlistRefundJobDetail) {
        if (!waitlistRefundJobEnabled) {
            return null;
        }
        
        return TriggerBuilder.newTrigger()
                .forJob(waitlistRefundJobDetail)
                .withIdentity("waitlistRefundTrigger", "waitlistJobs")
                .withDescription("Trigger for waitlist refund job - runs every 15 minutes")
                .withSchedule(CronScheduleBuilder.cronSchedule(waitlistRefundCronExpression)
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        scheduleExpiredPackageJob();
        scheduleWaitlistRefundJob();
    }

    private void scheduleExpiredPackageJob() {
        if (!expiredPackageJobEnabled) {
            log.info("ExpiredUserPackageJob is disabled. Not scheduling.");
            return;
        }

        try {
            JobDetail jobDetail = applicationContext.getBean("expiredUserPackageJobDetail", JobDetail.class);
            Trigger trigger = applicationContext.getBean("expiredUserPackageJobTrigger", Trigger.class);
            
            if (trigger == null) {
                log.warn("ExpiredUserPackageJob trigger is null. Job not scheduled.");
                return;
            }

            JobKey jobKey = jobDetail.getKey();
            if (!scheduler.checkExists(jobKey)) {
                scheduler.scheduleJob(jobDetail, trigger);
                log.info("Scheduled ExpiredUserPackageJob with cron expression: {}", expiredPackageCronExpression);
            } else {
                log.info("ExpiredUserPackageJob already exists. Skipping scheduling.");
            }
        } catch (Exception e) {
            log.error("Error scheduling ExpiredUserPackageJob", e);
        }
    }

    private void scheduleWaitlistRefundJob() {
        if (!waitlistRefundJobEnabled) {
            log.info("WaitlistRefundJob is disabled. Not scheduling.");
            return;
        }

        try {
            JobDetail jobDetail = applicationContext.getBean("waitlistRefundJobDetail", JobDetail.class);
            Trigger trigger = applicationContext.getBean("waitlistRefundJobTrigger", Trigger.class);
            
            if (trigger == null) {
                log.warn("WaitlistRefundJob trigger is null. Job not scheduled.");
                return;
            }

            JobKey jobKey = jobDetail.getKey();
            if (!scheduler.checkExists(jobKey)) {
                scheduler.scheduleJob(jobDetail, trigger);
                log.info("Scheduled WaitlistRefundJob with cron expression: {}", waitlistRefundCronExpression);
            } else {
                log.info("WaitlistRefundJob already exists. Skipping scheduling.");
            }
        } catch (Exception e) {
            log.error("Error scheduling WaitlistRefundJob", e);
        }
    }
}
