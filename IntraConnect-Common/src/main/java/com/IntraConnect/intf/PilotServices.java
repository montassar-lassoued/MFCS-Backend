package com.IntraConnect.intf;

import com.IntraConnect.command.handlerReg.Register;
import org.springframework.context.ApplicationContext;
import org.jdom2.Element;

public interface PilotServices {
	String getName();
	
	void configuration(Element root, ApplicationContext context);
	
	void register();
	
	void validate();
	
	void run();
	
	void stop();
	
}
