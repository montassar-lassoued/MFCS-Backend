package com.IntraConnect.intf;


import com.IntraConnect.command.handlerReg.Register;

public abstract class PilotApplicationServices implements PilotServices{
	
	public final Register register;
	
	protected PilotApplicationServices(Register register) {
		this.register = register;
	}
	
	@Override
	public abstract  void register() ;
}
