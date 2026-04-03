package com.IntraConnect.messageEngine;

import com.IntraConnect.controller.Controller;
import com.IntraConnect.intf.ContentServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class AbstractMessageEngine implements MessageEngine {
	private static final Logger log = LoggerFactory.getLogger(AbstractMessageEngine.class);
	@Autowired
	private ContentServices contentServices ;
	
	protected void handleIncomingData(Controller controller, byte[] data){
		if (controller != null) {
			contentServices.handleIncomingData(controller, data);
			log.info("receive from - {} - : {}", controller.getName(), new String(data));
		}
		else {
			log.info("receive - Unknown Controller: {}", new String(data));
		}
		
	}
	// Gemeinsame Logik für alle Protokolle
	protected byte[] prepareData(Controller controller, byte[] content) {
		byte[] prefix = controller.getPrefix().getBytes(StandardCharsets.UTF_8);
		byte[] suffix = controller.getSuffix().getBytes(StandardCharsets.UTF_8);
		
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
