package com.IntraConnect.datagram.server;

import com.IntraConnect.controller.Connectable;
import com.IntraConnect.messageEngine.AbstractConnectionEngine;
import com.IntraConnect.udpController.UdpController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.time.Instant;
import java.util.*;

@Component
public class UdpServerEngine extends AbstractConnectionEngine {
	
	private static final Logger log = LoggerFactory.getLogger(UdpServerEngine.class);
	private Selector selector;
	private final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
	@Autowired
	private TaskScheduler reconnectCtrlTaskScheduler; // Spring's interner Scheduler
	private final List<Connectable> disconnectedCtrl = new ArrayList<>();
	
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
	
	@Override
	protected void connect(Connectable connectable) {
		try {
			DatagramChannel channel = DatagramChannel.open();
			channel.bind(new InetSocketAddress(connectable.getPort()));
			channel.configureBlocking(false);
			
			// WICHTIG: Wir hängen das Controller-Objekt als Attachment an den Key!
			channel.register(selector, SelectionKey.OP_READ, connectable);
			
			controllerChannels.put(connectable.getName(), channel);
			log.info("UDP Server Controller '{}' listening on port {}", connectable.getName(), connectable.getPort());
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		
	
	}
	
	@Override
	protected void disconnect(Connectable connectable) {
		DatagramChannel channel = controllerChannels.remove(connectable.getName());
		
		if (channel != null) {
			try {
				// 1. Alle Keys im Selector finden, die diesen Channel nutzen
				for (SelectionKey key : selector.keys()) {
					if (key.channel() == channel) {
						key.cancel(); // Stoppt das Monitoring durch den Selector
						break;
					}
				}
				// 2. Den Channel physisch schließen (gibt den Port frei)
				channel.close();
				
				// 3. Selector aufwecken, damit der Loop die Änderungen sofort registriert
				selector.wakeup();
				
				log.info("UDP Controller '{}' wurde erfolgreich getrennt und Port freigegeben.", connectable.getName());
			} catch (IOException e) {
				log.error("Fehler beim Schließen des UDP Channels für {}", connectable.getName(), e);
			}
		} else {
			log.warn("Disconnect fehlgeschlagen: Kein aktiver UDP Channel für '{}' gefunden.", connectable.getName());
		}
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
							Connectable connectable = (Connectable) key.attachment();
							
							buffer.clear();
							SocketAddress clientAddr = channel.receive(buffer);
							if (clientAddr != null) {
								buffer.flip();
								byte[] data = new byte[buffer.limit()];
								buffer.get(data);
								
								// Aufruf der zentralen Methode in AbstractMessageEngine
								handleIncomingData(connectable, data);
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
	public boolean sendMessage(Connectable connectable, byte[] content) {
		DatagramChannel channel = controllerChannels.get(connectable.getName());
		if (channel == null) return false;
		
		try {
			byte[] data = prepareData(connectable, content);
			InetSocketAddress target = new InetSocketAddress(connectable.getHost(), connectable.getPort());
			int sent = channel.send(ByteBuffer.wrap(data), target);
			return sent == data.length;
		} catch (IOException e) {
			log.error("UDP Send Fehler für {}", connectable.getName(), e);
			return false;
		}
	}
	
	@Override
	public boolean supports(Connectable connectable) {
		return !connectable.isActive() && connectable instanceof UdpController;
	}
}

