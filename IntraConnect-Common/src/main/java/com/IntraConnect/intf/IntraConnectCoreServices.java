package com.IntraConnect.intf;

import com.IntraConnect.command.handlerReg.Register;

public abstract class IntraConnectCoreServices implements IntraConnectServices {
	
	protected final Register register;
	
	protected IntraConnectCoreServices(Register register) {
		this.register = register;
	}
	
	@Override
	public abstract void register() ;
}
