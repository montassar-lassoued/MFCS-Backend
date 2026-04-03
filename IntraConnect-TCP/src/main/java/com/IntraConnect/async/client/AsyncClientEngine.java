package com.IntraConnect.async.client;

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
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class AsyncClientEngine extends AbstractMessageEngine {
	
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private static final Logger log = LoggerFactory.getLogger(AsyncClientEngine.class);
	private final Map<String, TcpClientSession> clientSessions = new ConcurrentHashMap<>();
	
	public AsyncClientEngine() {

	}
	
	public void  connect(Controller controller){
		connectWithRetry(controller);
	}
	
	/** Connect zum Server */
	public void connectWithRetry(Controller controller) {
		try {
			AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();
			channel.connect(
					new InetSocketAddress(controller.getHost(),
							controller.getPort()),
					null,
					new CompletionHandler<Void, Void>() {
						@Override
						public void completed(Void result, Void attachment) {
							TcpClientSession session = new TcpClientSession(channel, controller);
							clientSessions.put(controller.getName(), session);
							log.info("Connected to {}:{}", controller.getHost(), controller.getPort());
							startRead(session);
						}
						
						@Override
						public void failed(Throwable exc, Void attachment) {
							if (exc instanceof java.net.ConnectException) {
								log.error("TCP Connect refused to {}:{} - Server offline?", controller.getHost(), controller.getPort(), exc);
							} else if (exc instanceof java.nio.channels.AsynchronousCloseException) {
								log.warn("Channel closed during connect to {}:{}", controller.getHost(), controller.getPort(), exc);
							} else {
								log.error("Connection failed to {}:{}:", controller.getHost(), controller.getPort(), exc);
							}
							scheduler.schedule(() -> connect(controller), 5, TimeUnit.SECONDS);
							
						}
					});
		}catch (IOException e) {
			log.error("Failed to open channel for {}:{}", controller.getHost(), controller.getPort(), e);
			
			// Auch hier Retry in 5s
			scheduler.schedule(() -> connect(controller), 5, TimeUnit.SECONDS);
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
	public boolean sendMessage(Controller controller, byte[] content) {
		TcpClientSession session = clientSessions.get(controller.getName());
		if (session == null) return false;
		if (content == null) return false;
		
		byte[] data = prepareData(controller, content); // Aus Basisklasse
		byte[] framed = addLengthPrefix(data);         // Aus Basisklasse
		session.send(framed);
		
		return true;
	}
	
	@Override
	public boolean supports(Controller controller) {
		// Logik: Ist ein Client-Controller UND nutzt TCP
		return controller.isActive() && controller instanceof TcpController;
	}
}
