package com.IntraConnect.async.server;

import com.IntraConnect.controller.Controller;
import com.IntraConnect.messageEngine.AbstractMessageEngine;
import com.IntraConnect.tcpController.TcpController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Component
public class AsyncServerEngine extends AbstractMessageEngine {
	
	private static final Logger log = LoggerFactory.getLogger(AsyncServerEngine.class);
	private final Map<String, List<TcpSession>> controllerSessions = new ConcurrentHashMap<>();
	private final java.nio.channels.AsynchronousChannelGroup channelGroup;
	
	public AsyncServerEngine() throws IOException {
		this.channelGroup = java.nio.channels.AsynchronousChannelGroup.withFixedThreadPool(
				Runtime.getRuntime().availableProcessors() * 2,
				Executors.defaultThreadFactory()
		);
	}
	
	/** Controller registrieren -> startet ServerSocketChannel auf dem Port */
	public void registerController(Controller controller) throws IOException {
		AsynchronousServerSocketChannel server =
				AsynchronousServerSocketChannel.open(channelGroup)
						.bind(new InetSocketAddress(controller.getPort()));
		accept(server, controller);
		log.info("Controller '{}' listening on port {}", controller.getName(), controller.getPort());
	}
	
	/** Accept neuer Clients */
	private void accept(AsynchronousServerSocketChannel server, Controller controller) {
		server.accept(null, new CompletionHandler<>() {
			@Override
			public void completed(AsynchronousSocketChannel client, Object att) {
				TcpSession session = new TcpSession(client, controller);
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
	public boolean supports(Controller controller) {
		// Logik: Ist ein Server-Controller UND nutzt TCP
		return !controller.isActive() && controller instanceof TcpController;
	}
	
	@Override
	public boolean sendMessage(Controller controller, byte[] content) {
		List<TcpSession> sessions = controllerSessions.get(controller.getName());
		if (sessions == null || sessions.isEmpty()) return false;
		
		byte[] data = prepareData(controller, content); // Aus Basisklasse
		byte[] framed = addLengthPrefix(data);         // Aus Basisklasse
		
		for (TcpSession session : sessions) {
			session.send(framed);
		}
		return true;
	}
}
