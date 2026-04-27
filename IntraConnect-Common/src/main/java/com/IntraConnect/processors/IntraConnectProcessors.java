package com.IntraConnect.processors;

import com.IntraConnect.intf.Processor;
import org.jdom2.Element;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class IntraConnectProcessors {

	private HashMap<String, Processor> processorRegistry;
	private HashMap<String, Processor> controllerProcessor;
	
	public void loadProcessors(Element systemConfig){
		processorRegistry = new HashMap<>();
		
		Element processorsElement = systemConfig.getChild("Processors");
		
		if (processorsElement == null) {
			throw new RuntimeException("No <Processors> defined");
		}
		
		for (Element p : processorsElement.getChildren("Processor")) {
			
			String name = p.getAttributeValue("name");
			String className = p.getAttributeValue("class");
			
			try {
				Class<?> clazz = Class.forName(className);
				Processor instance = (Processor) clazz.getDeclaredConstructor().newInstance();
				
				processorRegistry.put(name, instance);
				
			} catch (Exception e) {
				throw new RuntimeException("Failed to load processor: " + name, e);
			}
		}
		
		mapControllersToProcessors(systemConfig);
	}
	private void mapControllersToProcessors(Element systemConfig) {
		
		controllerProcessor = new HashMap<>();
		
		List<Element> modules = systemConfig
				.getChild("Modules")
				.getChildren("Module");
		
		for (Element module : modules) {
			
			List<Element> controllers = module.getChildren("Connectable");
			
			for (Element controller : controllers) {
				
				String controllerName = controller.getAttributeValue("name");
				
				Element processorElement = controller.getChild("Processor");
				
				if (processorElement == null) {
					throw new RuntimeException(
							"No processor defined for controller: " + controllerName
					);
				}
				
				String processorName = processorElement.getAttributeValue("name");
				
				Processor processor = processorRegistry.get(processorName);
				
				if (processor == null) {
					throw new RuntimeException(
							"Processor not found: " + processorName
					);
				}
				
				controllerProcessor.put(controllerName, processor);
			}
		}
	}
	
	public void handle(String controllerName, byte[] data){
		Processor processor = controllerProcessor.get(controllerName);
		
		if (processor == null) {
			throw new RuntimeException("No processor for controller: " + controllerName);
		}
		
		processor.process(data);
	}
}
