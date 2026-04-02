package com.IntraConnect.handler;

import com.IntraConnect.intf.Handler;
import com.IntraConnect.record.LoadUnitDestination;
import com.IntraConnect.record.LoadUnitLocationDestination;
import com.IntraConnect.utils.Utils;

public class LoadUnitSetLocationDestinationHandler implements Handler<LoadUnitLocationDestination> {
	@Override
	public void handle(LoadUnitLocationDestination payload) {
		if (payload != null &&
				!payload.LuNumber().isBlank() &&
				!payload.Location().isBlank() &&
				!payload.Destination().isBlank()){
			
			Utils.setLuLocationDestination(payload.LuNumber(), payload.Location(), payload.Destination());
		}
	}
}
