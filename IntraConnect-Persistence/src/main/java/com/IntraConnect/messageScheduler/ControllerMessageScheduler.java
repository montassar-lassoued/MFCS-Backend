package com.IntraConnect.messageScheduler;

import com.IntraConnect._enum.Transfer;
import com.IntraConnect.controller.Connectable;
import com.IntraConnect.messageEngine.ControllerRegistry;
import com.IntraConnect.messageEngine.ConnectionEngine;
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
	private final List<ConnectionEngine> connectionEngines;
	private final ControllerRegistry controllerRegistry;
	
	public ControllerMessageScheduler(List<ConnectionEngine> connectionEngines, ControllerRegistry controllerRegistry) {
		this.connectionEngines = connectionEngines;
		this.controllerRegistry = controllerRegistry;
	}
	
	@Scheduled(fixedRate = 2000, initialDelay = 5000)
	public void sendScheduledMessages() {
		Map<String, Connectable> controllerMap = controllerRegistry.getAllControllers().stream()
				.collect(Collectors.toMap(Connectable::getName, c -> c, (a, b) -> a));
		
		processMessages(controllerMap);
	}
	
	private void processMessages(Map<String, Connectable> controllerMap) {
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
				
				Connectable connectable = controllerMap.get(controllerName);
				if (connectable == null) {
					log.warn("Kein registrierter Controller gefunden: {}", controllerName);
					continue;
				}
				
				ConnectionEngine engine = connectionEngines.stream()
						.filter(e -> e.supports(connectable))
						.findFirst()
						.orElse(null);
				
				if (engine == null) {
					log.error("Keine Engine für Controller {} gefunden", controllerName);
					continue;
				}
				
				try {
					boolean success = engine.sendMessage(connectable, content);
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
