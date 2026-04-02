package com.IntraConnect.command.scheduler;

import com.IntraConnect.intf.Handler;

public final class Command<T> {
	private final Class<? extends Handler<?>> type;
	private final T payload;
	
	
	public Command( Class<? extends Handler<?>> type, T payload) {
		
		this.type = type;
		this.payload = payload;
	}
	
	public Class<? extends Handler<?>> getType() {
		return type;
	}
	
	public T getPayload() {
		return payload;
	}
}