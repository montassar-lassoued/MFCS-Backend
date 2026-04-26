package com.IntraConnect.intf;

import com.IntraConnect.command.handlerReg.Register;

public abstract class PilotCoreServices implements PilotServices {
	
	protected final Register register;
	
	protected PilotCoreServices(Register register) {
		this.register = register;
	}
	
	@Override
	public abstract void register() ;
}
