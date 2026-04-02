package com.IntraConnect.command.trigger;

import com.IntraConnect.command.scheduler.Command;
import com.IntraConnect.intf.CommandQueue;
import com.IntraConnect.intf.Handler;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * Manual Trigger*/

@Component
public class Trigger {
	
	private static Trigger instance;
	
	private final CommandQueue queue;
	
	public Trigger(CommandQueue queue) {
		this.queue = queue;
	}
	
	@PostConstruct
	private void init() {
		instance = this;
	}
	
	public static Trigger get() {
		if (instance == null) {
			throw new IllegalStateException("Trigger not initialized yet");
		}
		return instance;
	}
	
	public <T> void addQueue(Class<? extends Handler<T>> handlerType, T payload) {
		queue.enqueue(new Command<>(handlerType, payload));
	}
}
