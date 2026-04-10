package com.IntraConnect.datagram.client;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class UdpClientEngine extends AbstractConnectionEngine {
	
	private static final Logger log = LoggerFactory.getLogger(UdpClientEngine.class);
	private final DatagramChannel channel;
	private final ExecutorService pool;
	private final Map<SocketAddress, Connectable> servers = new ConcurrentHashMap<>();
	private final ByteBuffer buffer = ByteBuffer.allocate(1024);
	private final List<Connectable> disconnectedCtrl = new ArrayList<>();
	@Autowired
	private TaskScheduler reconnectCtrlTaskScheduler; // Spring's interner Scheduler
	
	
	public UdpClientEngine() throws Exception {
		channel = DatagramChannel.open();
		channel.configureBlocking(false);
		pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		startReceiveLoop();
	}
	
	@Override
	protected void connect(Connectable connectable) {
		InetSocketAddress addr = new InetSocketAddress(connectable.getHost(), connectable.getPort());
		servers.put(addr, connectable);
		System.out.println("Server hinzugefügt: " + connectable.getName());
	}
	
	@Override
	protected void disconnect(Connectable connectable) {
		Iterator<Map.Entry<SocketAddress, Connectable>> it = servers.entrySet().iterator();
		boolean removed = false;
		
		while (it.hasNext()) {
			Map.Entry<SocketAddress, Connectable> entry = it.next();
			String entryAddr = entry.getKey().toString(); // z.B. "/192.168.1.10:5000"
			
			// Prüfung auf Name oder die spezifische Remote-Adresse
			if (entry.getValue().getName().equals(connectable.getName())) {
				disconnectedCtrl.add(entry.getValue());
				it.remove();
				removed = true;
				log.info("UDP Client: Controller '{}' ({}) entfernt.", connectable.getName(), entryAddr);
			}
		}
		
		if (!removed) {
			log.warn("UDP Client: Kein Controller mit Name '{}' gefunden.", connectable.getName());
		}
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
								Connectable ctrl = servers.get(server);
								if (ctrl != null) { // Nur verarbeiten, wenn noch registriert!
									buffer.flip();
									byte[] data = new byte[buffer.limit()];
									buffer.get(data);
									pool.submit(() -> handleIncomingData(ctrl, data));
								} else {
									log.debug("Paket von unbekanntem Server ignoriert: {}", server);
								}
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
	public boolean sendMessage(Connectable connectable, byte[] content) {
		if (connectable == null) {
			log.warn("Controller not registered");
			return false;
		}
		InetSocketAddress target =
				new InetSocketAddress(connectable.getHost(), connectable.getPort());
		try {
			
			byte[] data = prepareData(connectable, content); // Aus Basisklasse
			
			ByteBuffer buffer = ByteBuffer.wrap(data);
			int sent = channel.send(buffer, target);
			
			return sent == data.length;
			
		} catch (IOException e) {
			log.error("Failed to send message to {}: {}", connectable, e.getMessage());
			return false;
		}
	}
	
	@Override
	public boolean supports(Connectable connectable) {
		// Logik: Ist ein Client-Controller UND nutzt UDP
		return connectable.isActive() && connectable instanceof UdpController;
	}
}
