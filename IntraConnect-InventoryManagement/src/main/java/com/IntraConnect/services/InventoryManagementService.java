package com.IntraConnect.services;

import com.IntraConnect.command.handlerReg.Register;
import com.IntraConnect.intf.PilotApplicationServices;
import org.jdom2.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class InventoryManagementService extends PilotApplicationServices {
	
	public InventoryManagementService(Register register){
		super(register);
		
	}
	@Override
	public String getName() {
		return "InventoryManagement";
	}
	
	@Override
	public void configuration(Element root, ApplicationContext context) {

	}
	
	@Override
	public void register() {

	}
	
	@Override
	public void validate() {
	
	}
	
	@Override
	public void run() {
	
	}
	
	@Override
	public void stop() {
	
	}
}
