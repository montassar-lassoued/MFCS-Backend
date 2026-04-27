package com.IntraConnect.intf;

import org.springframework.context.ApplicationContext;
import org.jdom2.Element;

public interface IntraConnectServices {
	String getName();
	
	void configuration(Element root, ApplicationContext context);
	
	void register();
	
	void validate();
	
	void run();
	
	void stop();
	
}
