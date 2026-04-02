package com.IntraConnect.command.scheduler;

import com.IntraConnect.command.handlerReg.Register;
import com.IntraConnect.command.handlerReg.HandlerConfig;
import com.IntraConnect.command.handlerReg.time.DailyTrigger;
import com.IntraConnect.command.handlerReg.time.FixedRateTrigger;
import com.IntraConnect.command.handlerReg.time.NumberRateTrigger;
import com.IntraConnect.command.handlerReg.time.OneTimeTrigger;
import com.IntraConnect.helper.Console;
import com.IntraConnect.intf.CommandQueue;
import com.IntraConnect.intf.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CommandScheduler {
	
	private static final Logger log = LoggerFactory.getLogger(CommandScheduler.class);
	private final Register register;
	private final ScheduledExecutorService scheduler =
			Executors.newScheduledThreadPool(2);
	
	private final CommandQueue queue;
	
	public CommandScheduler(Register register, CommandQueue queue) {
		this.register = register;
		this.queue = queue;
	}
	
	public void init() {
		log.info("Command-Scheduler running");
		for (Map.Entry<Class<? extends Handler<?>>, HandlerConfig> entry
				: register.getHandlers().entrySet()) {
			
			Class<? extends Handler<?>> handlerType = entry.getKey();
			HandlerConfig config = entry.getValue();
			
			if(config.time() instanceof OneTimeTrigger(long delaySeconds)){
				scheduleOnce(handlerType, delaySeconds);
			}
			else if(config.time() instanceof FixedRateTrigger(long intervalSeconds)){
				scheduleRepeated(handlerType, intervalSeconds);
			}
			else if(config.time() instanceof NumberRateTrigger(long intervalSeconds, int times)){
				repeatNTimes(handlerType, intervalSeconds,times);
			}
			else if(config.time() instanceof DailyTrigger(LocalTime time) ){
				scheduleDaily(handlerType, time);
			}
		}
	}
	
	public void scheduleOnce(Class<? extends Handler<?>> handlerType, long delaySeconds) {
		scheduler.schedule(() -> queue.enqueue(new Command<>(handlerType, null)),
				delaySeconds,
				TimeUnit.MILLISECONDS);
	}
	
	public void scheduleRepeated(Class<? extends Handler<?>> handlerType, long intervalSeconds) {
		scheduler.scheduleAtFixedRate(() -> {
			try {
				queue.enqueue(new Command<>(handlerType, null));
			} catch (Exception e) {
				log.error(e.getMessage()); // Loggen, aber Task weiter laufen lassen
			}
		}, 0, intervalSeconds, TimeUnit.MILLISECONDS);
	}
	
	// begrenzt wiederholt
	public void repeatNTimes(Class<? extends Handler<?>> handlerType, long intervalSeconds, int times) {
		
		AtomicInteger counter = new AtomicInteger(0);
		
		ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
			
			if (counter.incrementAndGet() > times) {
				return;
			}
			
			queue.enqueue(new Command<>(handlerType, null));
			
		}, 0, intervalSeconds, TimeUnit.SECONDS);
		
		scheduler.schedule(() -> future.cancel(false),
				intervalSeconds * times,
				TimeUnit.MILLISECONDS);
	}
	
	
	public void scheduleDaily(Class<? extends Handler<?>> handlerType, LocalTime time) {
		long initialDelay = computeInitialDelay(time);
		
		scheduler.scheduleAtFixedRate(() -> queue.enqueue(new Command<>(handlerType, null)),
				initialDelay,
				TimeUnit.DAYS.toSeconds(1),
				TimeUnit.MILLISECONDS);
	}
	
	private long computeInitialDelay(LocalTime time) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime nextRun = now.with(time);
		
		if (now.isAfter(nextRun)) {
			nextRun = nextRun.plusDays(1);
		}
		
		return Duration.between(now, nextRun).getSeconds();
	}
	
	public void stop() {
		// sauber stoppen:
		scheduler.shutdown();              // verhindert neue Tasks
		try {
			boolean terminate = scheduler.awaitTermination(5, TimeUnit.SECONDS); // wartet laufende Tasks
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
