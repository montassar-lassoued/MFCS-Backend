package com.IntraConnect.shutdown;

import com.IntraConnect.ModuleBootstrap;
import com.IntraConnect.command.scheduler.CommandScheduler;
import com.IntraConnect.helper.Console;
import com.IntraConnect.intf.PilotServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class shutdownListener implements ApplicationListener<ContextClosedEvent> {
	
	private static final Logger log = LoggerFactory.getLogger(shutdownListener.class);
	@Autowired
	private ModuleBootstrap moduleBootstrap;
	@Autowired
	private CommandScheduler SchedulerExecutor;
	
	@Override
	public void onApplicationEvent(@NonNull ContextClosedEvent event) {
		moduleBootstrap.shutdown();
		SchedulerExecutor.stop();
		log.info("System stopped... ");
		Console.info.println("System stopped... ");
	}
	

}
