package com.IntraConnect.schedulerConfig;


import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;


/**
 * TCP / UDP - Send Message
 * Registrierung eines ScheduledAnnotationBeanPostProcessor
 * Suche nach allen @Scheduled Methoden
 * Erzeugung eines TaskSchedulers
 * Planung der Jobs*/
@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {
	
	@Override
	public void configureTasks(ScheduledTaskRegistrar registrar) {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(4);
		scheduler.setThreadNamePrefix("intraConnect-scheduler-");
		scheduler.initialize();
		
		registrar.setTaskScheduler(scheduler);
	}
}
