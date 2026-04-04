package com.IntraConnect.messageScheduler;

import com.IntraConnect._enum.Transfer;
import com.IntraConnect.controller.Controller;
import com.IntraConnect.messageEngine.ControllerRegistry;
import com.IntraConnect.messageEngine.MessageEngine;
import com.IntraConnect.queryExec.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ControllerMessageScheduler {
	
	private static final Logger log = LoggerFactory.getLogger(ControllerMessageScheduler.class);
	
	// Spring injiziert hier automatisch alle Klassen, die MessageEngine implementieren
	private final List<MessageEngine> messageEngines;
	private final ControllerRegistry controllerRegistry;
	
	public ControllerMessageScheduler(List<MessageEngine> messageEngines, ControllerRegistry controllerRegistry) {
		this.messageEngines = messageEngines;
		this.controllerRegistry = controllerRegistry;
	}
	
	@Scheduled(fixedRate = 2000)
	public void sendScheduledMessages() {
		Map<String, Controller> controllerMap = controllerRegistry.getAllControllers().stream()
				.collect(Collectors.toMap(Controller::getName, c -> c, (a, b) -> a));
		
		processMessages(controllerMap);
	}
	
	private void processMessages(Map<String, Controller> controllerMap) {
		try (Transaction transaction = Transaction.create()) {
			
			// SQL-Query mit direkt eingebettetem Enum-Wert
			String selectSql = "SELECT T.ID, C.NAME, T.CONTENT FROM TRANSFER_OUT T " +
					"JOIN CONTROLLER C ON C.ID = T.CONTROLLER_ID " +
					"WHERE T.PROCESSED = ?";
			
			ResultSet rs = transaction.select(selectSql, Transfer.NEW);
			
			while (rs.next()) {
				int id = rs.getInt("ID");
				String controllerName = rs.getString("NAME");
				byte[] content = rs.getBytes("CONTENT");
				
				Controller controller = controllerMap.get(controllerName);
				if (controller == null) {
					log.warn("Kein registrierter Controller gefunden: {}", controllerName);
					continue;
				}
				
				MessageEngine engine = messageEngines.stream()
						.filter(e -> e.supports(controller))
						.findFirst()
						.orElse(null);
				
				if (engine == null) {
					log.error("Keine Engine für Controller {} gefunden", controllerName);
					continue;
				}
				
				try {
					boolean success = engine.sendMessage(controller, content);
					Transfer status = success ? Transfer.OK : Transfer.FAILED;
					
					// Update-String mit direkter Verkettung von Status und ID
					String updateSql = "UPDATE TRANSFER_OUT SET PROCESSED = ?" +
							"WHERE ID = " + id;
					transaction.update(updateSql, status);
					
				} catch (Exception e) {
					log.error("Fehler beim Verarbeiten der ID {}", id, e);
					// Im Fehlerfall auf FAILED setzen
					transaction.update("UPDATE TRANSFER_OUT SET PROCESSED = ? WHERE ID = ? ", Transfer.FAILED, id);
				}
			}
		} catch (Exception e) {
			log.error("Kritischer Fehler im Scheduler-Prozess", e);
		}
	}
}
