package com.IntraConnect.async.server;

import com.IntraConnect.controller.Controller;
import com.IntraConnect.services.content.TCPControllerContentService;
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
public class AsyncServerEngine {
	
	private static final Logger log = LoggerFactory.getLogger(AsyncServerEngine.class);
	private final Map<String, List<TcpSession>> controllerSessions = new ConcurrentHashMap<>();
	private final java.nio.channels.AsynchronousChannelGroup channelGroup;
	@Autowired
	private TCPControllerContentService controllerContentService ;
	
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
				handleMessage(session.getController(), data);
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
	
	/** Verarbeitung eingehender Nachrichten*/
	private void handleMessage(Controller controller, byte[] data) {
		if (controller != null) {
			controllerContentService.startHandleContent(controller, data);
		}
	}
	
	/** SendMessage -> Daten aus Tabelle, ControllerName -> Session finden */
	public boolean sendMessage(String controllerName, byte[] content) {
		if (content == null) return false;
		
		List<TcpSession> sessions = controllerSessions.get(controllerName);
		if (sessions == null || sessions.isEmpty()) {
			log.warn("Keine aktive Session für Controller: {}", controllerName);
			return false;
		}
		
		for (TcpSession session : sessions) {
			byte[] data = getDataSet(session.getController(), content);
			session.send(frameMessage(data));
			log.info("Send to {}: {}", controllerName,new String(content, StandardCharsets.UTF_8));
		}
		return true;
	}
	
	private byte[] getDataSet(Controller controller, byte[]content){
		byte[] prefix = controller.getPrefix().getBytes(StandardCharsets.UTF_8);
		byte[] suffix = controller.getSuffix().getBytes(StandardCharsets.UTF_8);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream(
				prefix.length + content.length + suffix.length
		);
		out.writeBytes(prefix);
		out.writeBytes(content);
		out.writeBytes(suffix);
		
		return out.toByteArray();
	}
	
	/** Framing: 4 Byte Length-Prefix */
	private byte[] frameMessage(byte[] data) {
		ByteBuffer buf = ByteBuffer.allocate(4 + data.length);
		buf.putInt(data.length);
		buf.put(data);
		buf.flip();
		return buf.array();
	}
}
