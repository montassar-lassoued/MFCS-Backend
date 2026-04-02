package com.IntraConnect.command.memorieQueue;

import com.IntraConnect.command.scheduler.Command;
import com.IntraConnect.intf.CommandQueue;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class InMemoryCommandQueue implements CommandQueue {
	
	private final BlockingQueue<Command<?>> queue =
			new LinkedBlockingQueue<>();
	
	@Override
	public void enqueue(Command<?> command) {
		queue.offer(command);
	}
	
	@Override
	public Command<?> dequeue() throws InterruptedException {
		return queue.take();
	}
}
