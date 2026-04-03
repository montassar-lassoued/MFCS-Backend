package com.IntraConnect.intf;

import com.IntraConnect.controller.Controller;

import java.util.List;

public interface ContentServices {
	void handleIncomingData(Controller _controller, byte[] data);
}
