package com.IntraConnect.async.client;

import com.IntraConnect.controller.Connectable;
import com.IntraConnect.messageEngine.AbstractConnectionEngine;
import com.IntraConnect.tcpController.TcpController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class TcpClientEngine extends AbstractConnectionEngine {
	
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private static final Logger log = LoggerFactory.getLogger(TcpClientEngine.class);
	private final Map<String, TcpClientSession> clientSessions = new ConcurrentHashMap<>();
	@Autowired
	private TaskScheduler reconnectCtrlTaskScheduler; // Spring's interner Scheduler
	
	
	@Override
	protected void  connect(Connectable connectable){
		connectWithRetry(connectable);
	}
	
	@Override
	protected void disconnect(Connectable connectable) {
		TcpClientSession session = clientSessions.get(connectable.getName());
		if (session != null) {
			try {
				session.getChannel().close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/** Connect zum Server */
	public void connectWithRetry(Connectable connectable) {
		try {
			AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
			channel.connect(
					new InetSocketAddress(connectable.getHost(),
							connectable.getPort()),
					null,
					new CompletionHandler<Void, Void>() {
						@Override
						public void completed(Void result, Void attachment) {
							TcpClientSession session = new TcpClientSession(channel, connectable);
							clientSessions.put(connectable.getName(), session);
							log.info("Connected to {}:{}", connectable.getHost(), connectable.getPort());
							startRead(session);
						}
						
						@Override
						public void failed(Throwable exc, Void attachment) {
							if (exc instanceof java.net.ConnectException) {
								log.error("TCP Connect refused to {}:{} - Server offline?", connectable.getHost(), connectable.getPort(), exc);
							} else if (exc instanceof java.nio.channels.AsynchronousCloseException) {
								log.warn("Channel closed during connect to {}:{}", connectable.getHost(), connectable.getPort(), exc);
							} else {
								log.error("Connection failed to {}:{}:", connectable.getHost(), connectable.getPort(), exc);
							}
							scheduler.schedule(() -> connect(connectable), 5, TimeUnit.SECONDS);
							
						}
					});
		}catch (IOException e) {
			log.error("Failed to open channel for {}:{}", connectable.getHost(), connectable.getPort(), e);
			
			// Auch hier Retry in 5s
			scheduler.schedule(() -> connect(connectable), 5, TimeUnit.SECONDS);
		}
	}
	
	/** Lesen vom Server */
	private void startRead(TcpClientSession session) {
		var buffer = java.nio.ByteBuffer.allocateDirect(1024);
		session.getChannel().read(buffer, buffer, new CompletionHandler<>() {
			@Override
			public void completed(Integer bytesRead, java.nio.ByteBuffer buf) {
				if (bytesRead == -1) return;
				buf.flip();
				byte[] data = new byte[bytesRead];
				buf.get(data);
				buf.clear();
				log.info("Received from server: {}", new String(data));
				
				/* Handelt eingehende Nachrichten*/
				handleIncomingData(session.getController(), data);
				
				session.getChannel().read(buf, buf, this);
			}
			
			@Override
			public void failed(Throwable exc, java.nio.ByteBuffer buf) {
				log.error(exc.getMessage());
			}
		});
	}
	
	@Override
	public boolean sendMessage(Connectable connectable, byte[] content) {
		TcpClientSession session = clientSessions.get(connectable.getName());
		if (session == null) return false;
		if (content == null) return false;
		
		byte[] data = prepareData(connectable, content); // Aus Basisklasse
		byte[] framed = addLengthPrefix(data);         // Aus Basisklasse
		session.send(framed);
		
		return true;
	}
	
	@Override
	public boolean supports(Connectable connectable) {
		// Logik: Ist ein Client-Controller UND nutzt TCP
		return connectable.isActive() && connectable instanceof TcpController;
	}
}
