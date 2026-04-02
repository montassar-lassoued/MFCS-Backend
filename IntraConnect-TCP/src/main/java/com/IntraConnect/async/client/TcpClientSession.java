package com.IntraConnect.async.client;

import com.IntraConnect.controller.Controller;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class TcpClientSession {
	private final AsynchronousSocketChannel channel;
	private final Controller controller;
	private final Queue<ByteBuffer> writeQueue = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean writing = new AtomicBoolean(false);
	
	public TcpClientSession(AsynchronousSocketChannel channel, Controller controller) {
		this.channel = channel;
		this.controller = controller;
	}
	
	public void send(byte[] data) {
		writeQueue.offer(ByteBuffer.wrap(data));
		tryFlush();
	}
	
	private void tryFlush() {
		if (!writing.compareAndSet(false, true)) return;
		ByteBuffer buffer = writeQueue.poll();
		if (buffer == null) { writing.set(false); return; }
		
		channel.write(buffer, buffer, new CompletionHandler<>() {
			@Override
			public void completed(Integer result, ByteBuffer buf) {
				if (buf.hasRemaining()) channel.write(buf, buf, this);
				else { writing.set(false); tryFlush(); }
			}
			
			@Override
			public void failed(Throwable exc, ByteBuffer buf) {
				writing.set(false); tryFlush();
			}
		});
	}
	
	public AsynchronousSocketChannel getChannel() { return channel; }
	public Controller getController() { return controller; }
}
