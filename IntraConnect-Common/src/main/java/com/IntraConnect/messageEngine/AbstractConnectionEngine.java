package com.IntraConnect.messageEngine;

import com.IntraConnect.controller.Connectable;
import com.IntraConnect.intf.ContentServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Component
public abstract class AbstractConnectionEngine implements ConnectionEngine {
	private static final Logger log = LoggerFactory.getLogger(AbstractConnectionEngine.class);
	private ContentServices contentServices ;
	@Autowired
	private TaskScheduler taskScheduler;
	
	protected abstract void connect(Connectable connectable);
	protected abstract void disconnect(Connectable connectable);
	
	
	@Override
	public final void doConnect(Connectable connectable) throws Exception {
	
	}
	
	@Override
	public final void doDisconnect(Connectable connectable) {
		disconnect(connectable);
	   scheduleReconnect(connectable);
	}
	
	protected void scheduleReconnect(Connectable connectable) {
		log.info("Reconnect-Timer gestartet für: {}", connectable.getName());
		
		taskScheduler.schedule(() -> {
			try {
				// Nur versuchen, wenn das Flag noch aktiv ist
				connect(connectable);
				log.info("Reconnect erfolgreich für {}", connectable.getName());
				
			} catch (Exception e) {
				log.warn("Wiederverbindung fehlgeschlagen für {}, versuche es erneut...", connectable.getName());
				scheduleReconnect(connectable); // Rekursion für Endlos-Versuch
			}
		}, java.time.Instant.now().plusSeconds(5));
	}
	
	protected void handleIncomingData(Connectable connectable, byte[] data){
		if (connectable != null) {
			contentServices.handleIncomingData(connectable, data);
			log.info("receive from - {} - : {}", connectable.getName(), new String(data));
		}
		else {
			log.info("receive - Unknown Controller: {}", new String(data));
		}
		
	}
	// Gemeinsame Logik für alle Protokolle
	protected byte[] prepareData(Connectable connectable, byte[] content) {
		byte[] prefix = connectable.getPrefix().getBytes(StandardCharsets.UTF_8);
		byte[] suffix = connectable.getSuffix().getBytes(StandardCharsets.UTF_8);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			out.write(prefix);
			out.write(content);
			out.write(suffix);
		} catch (IOException e) {
			return content; // Fallback
		}
		return out.toByteArray();
	}
	
	// TCP braucht oft 4-Byte-Length, UDP meistens nicht.
	// Das kann hier optional angeboten werden.
	protected byte[] addLengthPrefix(byte[] data) {
		ByteBuffer buf = ByteBuffer.allocate(4 + data.length);
		buf.putInt(data.length);
		buf.put(data);
		return buf.array();
	}
}
