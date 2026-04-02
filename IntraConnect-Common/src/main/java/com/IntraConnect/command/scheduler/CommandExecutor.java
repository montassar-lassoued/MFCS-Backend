package com.IntraConnect.command.scheduler;

import com.IntraConnect.command.handlerReg.Register;
import com.IntraConnect.intf.CommandQueue;
import com.IntraConnect.intf.Handler;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.instrument.IllegalClassFormatException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Executor Commands for Trigger*/
@Component
public class CommandExecutor {
	
	private static final Logger log = LoggerFactory.getLogger(CommandExecutor.class);
	private final ExecutorService worker = Executors.newSingleThreadExecutor();
	private final Register registry;
	private final CommandQueue queue;
	
	public CommandExecutor(Register registry, CommandQueue queue) {
		this.registry = registry;
		this.queue = queue;
	}
	
	@PostConstruct
	public void start() {
		worker.submit(() -> {
			log.info("Worker started");
			while (!Thread.currentThread().isInterrupted()) {
				try {
					execute(queue.dequeue());
				} catch (Exception e) {
					log.error("Command execution failed", e);
				}
			}
			log.warn("Worker thread terminated");
		});
	}

	
	private <T> void execute(Command<T> command) {
		try {
			Handler<T> handler = registry.getHandler(command.getType());
			handler.handle(command.getPayload());
		} catch (Exception e) {
			throw new IllegalStateException("Handler not registered "+ command.getType().getName());
		}
	}
}
