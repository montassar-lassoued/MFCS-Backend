package com.IntraConnect;

import com.IntraConnect._enum.LifecyclePhase;
import com.IntraConnect.helper.Console;
import com.IntraConnect.intf.PilotServices;
import com.IntraConnect.processors.IntraConnectProcessors;
import com.IntraConnect.processors.ProcessorFactory;
import org.jdom2.Element;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.IntraConnect._enum.LifecyclePhase.*;

@Component
public class ModuleBootstrap {
	
	private final Element systemConfig;
	private final List<PilotServices> pilotServices;
	private final ApplicationContext context;
	private Map<PilotServices, Element> moduleConfigMap;
	private Map<String, PilotServices> registry;
	List<PilotServices> activeModules;
	//private final IntraConnectProcessors intraConnectProcessors;
	private final ProcessorFactory processorFactory;
	
	public ModuleBootstrap(
			Element systemConfig,
			List<PilotServices> pilotServices,
			ApplicationContext context,
			ProcessorFactory processorFactory) {
		
		this.systemConfig = systemConfig;
		this.pilotServices = pilotServices;
		this.context = context;
		this.processorFactory = processorFactory;
	}
	
	public void run() throws RuntimeException{
		
		buildRegistry();
		
		activeModules = resolveActiveModules();
		
		// PHASE 1 – CONFIGURE
		for (PilotServices module : activeModules) {
			Console.info.println("Configuring: " + module.getName());
			executePhase(CONFIGURE, module);
			Console.info.println(" - OK");
			Console.info.println("Validate: " + module.getName());
			executePhase(VALIDATE, module);
			executePhase(REGISTER, module);
			Console.info.println(" - OK");
		}
		
		
		//*********************
		// -------------------------
		// PHASE 1.5 – PROCESSOR INIT
		// -------------------------
		Console.info.println("Initializing Processors...");
		processorFactory.initialize(systemConfig);
		Console.info.println("Processors ready.");
		
		// PHASE 2 – RUN
		for (PilotServices module : activeModules) {
			Console.info.println("Running: " + module.getName());
			executePhase(RUN, module);
			Console.info.println(" - OK");
		}
	}
	
	public void shutdown(){
		// PHASE – SHUTDOWN
		for (PilotServices module : activeModules) {
			Console.info.println("Shutdown: " + module.getName());
			executePhase(SHUTDOWN, module);
			Console.info.println(" - OK");
		}
	}
	
	private void executePhase(LifecyclePhase phase, PilotServices module){
		switch (phase){
			case CONFIGURE -> module.configuration(moduleConfigMap.get(module), context);
			case VALIDATE -> module.validate();
			case REGISTER -> module.register();
			case RUN -> module.run();
			case SHUTDOWN -> module.stop();
		}
	}
	
	private List<PilotServices> resolveActiveModules() throws RuntimeException{
		
		List<PilotServices> result = new ArrayList<>();
		moduleConfigMap = new LinkedHashMap<>();
		
		List<Element> modules = systemConfig
				.getChild("Modules")
				.getChildren("Module");
		
		for (Element moduleElement : modules) {
			
			String name = moduleElement.getAttributeValue("name");
			boolean enabled = Boolean.parseBoolean(moduleElement.getAttributeValue("enabled"));
			
			if (!enabled) {
				continue;
			}
			
//			PilotServices service = registry.get(name.toLowerCase());
//			if (service == null) {
//				System.out.println(Thread.currentThread().getName());
//				Console.error.println("FATAL: No PilotService registered for: " + name);
//				System.exit(1);
//			}
//
//			result.add(service);
//			moduleConfigMap.put(service, moduleElement);
		}
		
		return result;
	}
	
	private void buildRegistry() {
		registry = pilotServices.stream()
				.collect(Collectors.toMap(
						s -> s.getName().toLowerCase(Locale.ROOT),
						Function.identity()
				));
	}
}
