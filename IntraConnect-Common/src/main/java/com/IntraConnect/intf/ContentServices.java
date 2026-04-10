package com.IntraConnect.intf;

import com.IntraConnect.controller.Connectable;

public interface ContentServices {
	void handleIncomingData(Connectable _connectable, byte[] data);
}
