package com.IntraConnect.async.server;

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
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Component
public class TcpServerEngine extends AbstractConnectionEngine {
	
	private static final Logger log = LoggerFactory.getLogger(TcpServerEngine.class);
	private final Map<String, List<TcpSession>> controllerSessions = new ConcurrentHashMap<>();
	private final java.nio.channels.AsynchronousChannelGroup channelGroup;
	@Autowired
	private TaskScheduler reconnectCtrlTaskScheduler; // Spring's interner Scheduler
	
	public TcpServerEngine() throws IOException {
		this.channelGroup = java.nio.channels.AsynchronousChannelGroup.withFixedThreadPool(
				Runtime.getRuntime().availableProcessors() * 2,
				Executors.defaultThreadFactory()
		);
	}
	
	@Override
	protected void connect(Connectable connectable) {
		try {
			AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(channelGroup)
					.bind(new InetSocketAddress(connectable.getPort()));
			
			accept(server, connectable);
			log.info("Controller '{}' listening on port {}", connectable.getName(), connectable.getPort());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected void disconnect(Connectable connectable) {
		List<TcpSession> sessions = controllerSessions.get(connectable.getName());
		if (sessions != null) {
			sessions.stream()
					.findFirst()
					.ifPresent(this::closeSession);
		}
	}

	/** Accept neuer Clients */
	private void accept(AsynchronousServerSocketChannel server, Connectable connectable) {
		server.accept(null, new CompletionHandler<>() {
			@Override
			public void completed(AsynchronousSocketChannel client, Object att) {
				TcpSession session = new TcpSession(client, connectable);
				registerSession(session);
				startRead(session);
				server.accept(null, this);
			}
			
			@Override
			public void failed(Throwable exc, Object att) {
				log.error("Accept failed", exc);
				server.accept(null, this);
			}
		});
	}
	
	/** Lesen von Client */
	private void startRead(TcpSession session) {
		AsynchronousSocketChannel client = session.getChannel();
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
		
		client.read(buffer, buffer, new CompletionHandler<>() {
			@Override
			public void completed(Integer bytesRead, ByteBuffer buf) {
				if (bytesRead == -1) {
					closeSession(session);
					return;
				}
				
				buf.flip();
				byte[] data = new byte[bytesRead];
				buf.get(data);
				buf.clear();
				
				log.info("Received from {}: {}", session.getController().getName(), new String(data));
				
				/* Handelt eingehende Nachrichten*/
				handleIncomingData(session.getController(), data);
				
				client.read(buf, buf, this);
			}
			
			@Override
			public void failed(Throwable exc, ByteBuffer buf) {
				closeSession(session);
			}
		});
	}
	
	/** Session speichern */
	public void registerSession(TcpSession session) {
		controllerSessions.computeIfAbsent(
				session.getController().getName(),
				k -> new ArrayList<>()
		).add(session);
	}
	
	/** Session schließen */
	private void closeSession(TcpSession session) {
		try {
			session.getChannel().close();
		} catch (Exception ignored) {}
		controllerSessions.getOrDefault(session.getController().getName(), new ArrayList<>()).remove(session);
	}
	
	@Override
	public boolean supports(Connectable connectable) {
		// Logik: Ist ein Server-Controller UND nutzt TCP
		return !connectable.isActive() && connectable instanceof TcpController;
	}
	
	@Override
	public boolean sendMessage(Connectable connectable, byte[] content) {
		List<TcpSession> sessions = controllerSessions.get(connectable.getName());
		if (sessions == null || sessions.isEmpty()) return false;
		
		byte[] data = prepareData(connectable, content); // Aus Basisklasse
		byte[] framed = addLengthPrefix(data);         // Aus Basisklasse
		
		for (TcpSession session : sessions) {
			session.send(framed);
		}
		return true;
	}
}
