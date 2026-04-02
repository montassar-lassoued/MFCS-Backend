package com.IntraConnect.datagram.client;

import com.IntraConnect.services.content.UDPControllerContentService;
import com.IntraConnect.controller.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class UdpClientEngine {
	
	private static final Logger log = LoggerFactory.getLogger(UdpClientEngine.class);
	private final DatagramChannel channel;
	private final ExecutorService pool;
	private final Map<SocketAddress, Controller> servers = new ConcurrentHashMap<>();
	private final ByteBuffer buffer = ByteBuffer.allocate(1024);
	@Autowired
	private UDPControllerContentService controllerContentService ;
	
	public UdpClientEngine() throws Exception {
		channel = DatagramChannel.open();
		channel.configureBlocking(false);
		pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		startReceiveLoop(); // Schleife läuft automatisch
	}
	
	// Server registrieren / verbinden (UDP = verbindungslos)
	public void registerController(Controller controller) throws Exception {
		InetSocketAddress addr = new InetSocketAddress(controller.getHost(), controller.getPort());
		servers.put(addr, controller);
		System.out.println("Server hinzugefügt: " + controller.getName());
	}
	
	// Asynchrone Empfangsschleife
	private void startReceiveLoop() {
		new Thread(() -> {
			try {
				Selector selector = Selector.open();
				channel.register(selector, SelectionKey.OP_READ);
				
				while (true) {
					selector.select();
					Iterator<SelectionKey> it = selector.selectedKeys().iterator();
					while (it.hasNext()) {
						SelectionKey key = it.next();
						it.remove();
						
						if (key.isReadable()) {
							buffer.clear();
							SocketAddress server = channel.receive(buffer);
							if (server != null) {
								buffer.flip();
								byte[] data = new byte[buffer.limit()];
								buffer.get(data);
								
								// asynchron verarbeiten
								pool.submit(() -> handleMessage(server, data));
							}
						}
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}, "UDP-Client-Receiver").start();
	}
	
	/** Zentraler Send-Pfad für Scheduler */
	public boolean sendMessage(Controller controller, byte[] content) {
		if (controller == null) {
			log.warn("Controller not registered");
			return false;
		}
		InetSocketAddress target =
				new InetSocketAddress(controller.getHost(), controller.getPort());
		try {
			
			byte[] data = getDataSet(controller, content);
			
			ByteBuffer buffer = ByteBuffer.wrap(data);
			int sent = channel.send(buffer, target);
			
			return sent == data.length;
			
		} catch (IOException e) {
			log.error("Failed to send message to {}: {}", controller, e.getMessage());
			return false;
		}
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
	
	// Verarbeitung eingehender Nachrichten
	private void handleMessage(SocketAddress server, byte[] data) {
		Controller controller = servers.get(server);
		if (controller != null) {
			controllerContentService.startHandleContent(controller, data);
		} else {
			log.error("{}:{}", server, Arrays.toString(data));
		}
	}
}
