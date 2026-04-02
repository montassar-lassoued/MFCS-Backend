package com.IntraConnect.processors;

import com.IntraConnect.intf.Processor;
import org.jdom2.Element;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;

@Component
public class ProcessorFactory {
	
	private final AutowireCapableBeanFactory beanFactory;
	
	private final Map<String, Processor> processorRegistry = new HashMap<>();
	private final Map<String, Processor> controllerProcessorMap = new HashMap<>();
	
	public ProcessorFactory(AutowireCapableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
	
	// 👉 wird einmal beim Startup aufgerufen
	public void initialize(Element systemConfig) {
		loadProcessors(systemConfig);
		mapControllers(systemConfig);
	}
	
	// ----------------------------
	// Processor laden
	// ----------------------------
	private void loadProcessors(Element systemConfig) {
		
		Element processorsElement = systemConfig.getChild("Processors");
		
		if (processorsElement == null) {
			throw new RuntimeException("No <Processors> defined");
		}
		
		for (Element p : processorsElement.getChildren("Processor")) {
			
			String name = p.getAttributeValue("name");
			String className = p.getAttributeValue("class");
			
			try {
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				java.util.Enumeration<java.net.URL> resources = cl.getResources("com/IntraConnect/processors/Processor_tcp_Class_1.class");
				while (resources.hasMoreElements()) {
					System.out.println(resources.nextElement());
				}
				Class<?> clazz = Class.forName(className);

				Processor instance =
						(Processor) beanFactory.createBean(clazz);

				processorRegistry.put(name.toLowerCase(), instance);
				
			} catch (Exception e) {
				throw new RuntimeException("Failed to load processor: " + name, e);
			}
		}
	}
	
	// ----------------------------
	// Controller → Processor Mapping
	// ----------------------------
	private void mapControllers(Element systemConfig) {
		
		List<Element> modules = systemConfig
				.getChild("Modules")
				.getChildren("Module");
		
		for (Element module : modules) {
			
			for (Element controller : module.getChildren("Controller")) {
				
				String controllerName = controller.getAttributeValue("name");
				
				Element processorElement = controller.getChild("Processor");
				
				if (processorElement == null) {
					throw new RuntimeException(
							"No processor defined for controller: " + controllerName
					);
				}
				
				String processorName =
						processorElement.getAttributeValue("name");
				
				Processor processor =
						processorRegistry.get(processorName.toLowerCase());
				
				if (processor == null) {
					throw new RuntimeException(
							"Processor not found: " + processorName
					);
				}
				
				controllerProcessorMap.put(controllerName, processor);
			}
		}
	}

	// ----------------------------
	// Runtime Zugriff
	// ----------------------------
	public Processor getProcessor(String controllerName) {
		
		Processor processor = controllerProcessorMap.get(controllerName);
		
		if (processor == null) {
			throw new RuntimeException(
					"No processor for controller: " + controllerName
			);
		}
		
		return processor;
	}
}
