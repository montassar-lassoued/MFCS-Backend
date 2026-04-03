package com.IntraConnect.datagram.server;

import com.IntraConnect.controller.Controller;
import com.IntraConnect.controllerContent.ControllerContentService;
import com.IntraConnect.intf.ContentServices;
import com.IntraConnect.messageEngine.AbstractMessageEngine;
import com.IntraConnect.udpController.UdpController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class UdpServerEngine extends AbstractMessageEngine {
	
	private static final Logger log = LoggerFactory.getLogger(UdpServerEngine.class);
	private Selector selector;
	private final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
	
	// Name -> Channel Mapping für das Senden
	private final Map<String, DatagramChannel> controllerChannels = new HashMap<>();
	
	public UdpServerEngine() {
		try {
			this.selector = Selector.open();
			startServerThread(); // Startet den Loop im Hintergrund
		} catch (IOException e) {
			log.error("UDP Selector konnte nicht geöffnet werden", e);
		}
	}
	
	public void registerController(Controller controller) throws IOException {
		DatagramChannel channel = DatagramChannel.open();
		channel.bind(new InetSocketAddress(controller.getPort()));
		channel.configureBlocking(false);
		
		// WICHTIG: Wir hängen das Controller-Objekt als Attachment an den Key!
		channel.register(selector, SelectionKey.OP_READ, controller);
		
		controllerChannels.put(controller.getName(), channel);
		log.info("UDP Server Controller '{}' listening on port {}", controller.getName(), controller.getPort());
	}
	
	private void startServerThread() {
		Thread serverThread = new Thread(() -> {
			try {
				while (!Thread.currentThread().isInterrupted()) {
					if (selector.select() == 0) continue;
					
					Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
					while (keys.hasNext()) {
						SelectionKey key = keys.next();
						keys.remove();
						
						if (key.isReadable()) {
							DatagramChannel channel = (DatagramChannel) key.channel();
							// Hier holen wir den Controller aus dem Attachment zurück!
							Controller controller = (Controller) key.attachment();
							
							buffer.clear();
							SocketAddress clientAddr = channel.receive(buffer);
							if (clientAddr != null) {
								buffer.flip();
								byte[] data = new byte[buffer.limit()];
								buffer.get(data);
								
								// Aufruf der zentralen Methode in AbstractMessageEngine
								handleIncomingData(controller, data);
							}
						}
					}
				}
			} catch (IOException e) {
				log.error("Fehler im UDP Server Loop", e);
			}
		}, "UDP-Server-Receiver");
		serverThread.setDaemon(true);
		serverThread.start();
	}
	
	@Override
	public boolean sendMessage(Controller controller, byte[] content) {
		DatagramChannel channel = controllerChannels.get(controller.getName());
		if (channel == null) return false;
		
		try {
			byte[] data = prepareData(controller, content);
			InetSocketAddress target = new InetSocketAddress(controller.getHost(), controller.getPort());
			int sent = channel.send(ByteBuffer.wrap(data), target);
			return sent == data.length;
		} catch (IOException e) {
			log.error("UDP Send Fehler für {}", controller.getName(), e);
			return false;
		}
	}
	
	@Override
	public boolean supports(Controller controller) {
		return !controller.isActive() && controller instanceof UdpController;
	}
}

