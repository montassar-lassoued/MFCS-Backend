package com.IntraConnect.datagram.server;

import com.IntraConnect.controller.Controller;
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
import java.util.Map;

@Component
public class UdpServerEngine {
	
	private static final Logger log = LoggerFactory.getLogger(UdpServerEngine.class);
	
	private Selector selector;
	private ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
	
	// ControllerName -> DatagramChannel + Port
	private final Map<String, DatagramChannel> controllerChannels = new HashMap<>();
	
	public void registerController(Controller controller) throws IOException {
		if (selector == null) selector = Selector.open();
		
		DatagramChannel channel = DatagramChannel.open();
		channel.bind(new InetSocketAddress(controller.getPort()));
		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_READ, controller.getPort());
		
		controllerChannels.put(controller.getName(), channel);
		
		log.info("UDP Controller '{}' listening on port {}", controller.getName(), controller.getPort());
	}
	
	/** Hauptloop, Non-Blocking */
	public void serverLoop() {
		try {
			while (true) {
				selector.select(); // block until at least one channel is ready
				Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
				
				while (keys.hasNext()) {
					SelectionKey key = keys.next();
					keys.remove();
					
					if (key.isReadable()) {
						DatagramChannel channel = (DatagramChannel) key.channel();
						buffer.clear();
						
						SocketAddress client = channel.receive(buffer);
						if (client != null) {
							buffer.flip();
							byte[] data = new byte[buffer.limit()];
							buffer.get(data);
							
							handleMessage(client, data, channel);
						}
					}
				}
			}
		} catch (IOException e) {
			log.error("UDP server loop error", e);
		}
	}
	
	/** Nachrichtenverarbeitung */
	private void handleMessage(SocketAddress client, byte[] data, DatagramChannel channel) {
		String msg = new String(data);
		log.info("Received from {}: {}", client, msg);
		
		// Optional: Echo zurück
		try {
			channel.send(ByteBuffer.wrap(("ACK: " + msg).getBytes()), client);
		} catch (IOException e) {
			log.error("Failed to send ACK", e);
		}
	}
	
	/** Zentraler Send-Pfad für Scheduler */
	public boolean sendMessage(Controller controller, byte[] content) {
		DatagramChannel channel = controllerChannels.get(controller.getName());
		if (channel == null) {
			log.warn("Controller {} not registered", controller);
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
}
