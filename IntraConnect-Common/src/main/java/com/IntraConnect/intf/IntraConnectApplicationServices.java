package com.IntraConnect.intf;


import com.IntraConnect.command.handlerReg.Register;

public abstract class IntraConnectApplicationServices implements IntraConnectServices {
	
	public final Register register;
	
	protected IntraConnectApplicationServices(Register register) {
		this.register = register;
	}
	
	@Override
	public abstract  void register() ;
}
