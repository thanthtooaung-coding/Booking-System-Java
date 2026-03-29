package com.alvin.bookingsystem.config;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import java.util.Properties;

@Configuration
public class QuartzConfig {

    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        return new SpringBeanJobFactory();
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(SpringBeanJobFactory springBeanJobFactory) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        
        int cpuCores = Runtime.getRuntime().availableProcessors();
        int optimalThreadPoolSize = Math.max(2, cpuCores * 2);
        
        Properties quartzProperties = new Properties();
        quartzProperties.put("org.quartz.scheduler.instanceName", "BookingSystemScheduler");
        quartzProperties.put("org.quartz.scheduler.instanceId", "AUTO");
        
        quartzProperties.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        quartzProperties.put("org.quartz.threadPool.threadCount", String.valueOf(optimalThreadPoolSize));
        quartzProperties.put("org.quartz.threadPool.threadPriority", "5");
        quartzProperties.put("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", "true");
        
        quartzProperties.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        
        quartzProperties.put("org.quartz.plugin.triggHistory.class", "org.quartz.plugins.history.LoggingTriggerHistoryPlugin");
        quartzProperties.put("org.quartz.plugin.triggHistory.triggerFiredMessage", "Trigger {1}.{0} fired job {6}.{5} at: {4, date, HH:mm:ss MM/dd/yyyy}");
        quartzProperties.put("org.quartz.plugin.triggHistory.triggerCompleteMessage", "Trigger {1}.{0} completed firing job {6}.{5} at {4, date, HH:mm:ss MM/dd/yyyy} with resulting trigger instruction code: {9}");
        
        factory.setQuartzProperties(quartzProperties);
        factory.setJobFactory(springBeanJobFactory);
        factory.setWaitForJobsToCompleteOnShutdown(true);
        factory.setOverwriteExistingJobs(true);
        
        return factory;
    }

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        scheduler.start();
        return scheduler;
    }
}
