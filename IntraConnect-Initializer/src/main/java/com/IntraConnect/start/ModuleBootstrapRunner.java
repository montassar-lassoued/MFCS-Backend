package com.IntraConnect.start;

import com.IntraConnect.ModuleBootstrap;
import com.IntraConnect.command.scheduler.CommandScheduler;
import com.IntraConnect.helper.Console;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ModuleBootstrapRunner implements ApplicationRunner {
	
	@Autowired
	private ModuleBootstrap moduleBootstrap;
	@Autowired
	private CommandScheduler SchedulerExecutor;
	
	@Override
	public void run(ApplicationArguments args) {
	
		moduleBootstrap.run();
		
		Console.info.println("System is running... ");
		SchedulerExecutor.init();
	}
}
