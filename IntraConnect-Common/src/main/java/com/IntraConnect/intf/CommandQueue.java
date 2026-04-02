package com.IntraConnect.intf;

import com.IntraConnect.command.scheduler.Command;

public interface CommandQueue {
	void enqueue(Command<?> command);
	Command<?> dequeue() throws InterruptedException;
}

