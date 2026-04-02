package com.IntraConnect.handler;

import com.IntraConnect.intf.Handler;
import com.IntraConnect.record.LoadUnitLocation;
import com.IntraConnect.utils.Utils;

public class LoadUnitSetLocationHandler implements Handler<LoadUnitLocation> {
	@Override
	public void handle(LoadUnitLocation payload) {
		if (payload != null &&
				!payload.LuNumber().isBlank() &&
				!payload.Location().isBlank()){
			
			Utils.setLuLocation(payload.LuNumber(), payload.Location());
		}
	}
}
