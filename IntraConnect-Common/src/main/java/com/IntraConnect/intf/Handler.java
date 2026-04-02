package com.IntraConnect.intf;

public interface Handler<T> {
	void handle(T payload);
}
