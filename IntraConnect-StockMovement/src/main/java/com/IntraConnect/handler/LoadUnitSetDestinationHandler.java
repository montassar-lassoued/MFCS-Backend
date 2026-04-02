package com.IntraConnect.handler;

import com.IntraConnect.intf.Handler;
import com.IntraConnect.record.LoadUnitDestination;
import com.IntraConnect.utils.Utils;

public class LoadUnitSetDestinationHandler implements Handler<LoadUnitDestination> {
	@Override
	public void handle(LoadUnitDestination payload) {
		if (payload != null &&
				!payload.LuNumber().isBlank() &&
				!payload.Destination().isBlank()){
			
			Utils.setLuDestination(payload.LuNumber(), payload.Destination());
		}
	}
}
