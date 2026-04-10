package com.IntraConnect.async.server;

import com.IntraConnect.controller.Connectable;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class TcpSession {
	
	private final AsynchronousSocketChannel channel;
	private final Connectable connectable;
	private final Queue<ByteBuffer> writeQueue = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean writing = new AtomicBoolean(false);
	
	public TcpSession(AsynchronousSocketChannel channel, Connectable connectable) {
		this.channel = channel;
		this.connectable = connectable;
	}
	
	public AsynchronousSocketChannel getChannel() { return channel; }
	public Connectable getController() { return connectable; }
	
	/** Thread-safe Send mit Write-Queue */
	public void send(byte[] data) {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		writeQueue.offer(buffer);
		tryFlush();
	}
	
	private void tryFlush() {
		if (!writing.compareAndSet(false, true)) return;
		
		ByteBuffer buffer = writeQueue.poll();
		if (buffer == null) {
			writing.set(false);
			return;
		}
		
		channel.write(buffer, buffer, new CompletionHandler<>() {
			@Override
			public void completed(Integer result, ByteBuffer buf) {
				if (buf.hasRemaining()) {
					channel.write(buf, buf, this);
				} else {
					writing.set(false);
					tryFlush();
				}
			}
			
			@Override
			public void failed(Throwable exc, ByteBuffer buf) {
				writing.set(false);
				tryFlush();
			}
		});
	}
}
