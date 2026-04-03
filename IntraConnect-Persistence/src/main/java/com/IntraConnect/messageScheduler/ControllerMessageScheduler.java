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
		// Wir holen uns alle Controller, die Nachrichten erwarten könnten
		List<Controller> activeControllers = controllerRegistry.getAllControllers();
		
		for (Controller controller : activeControllers) {
			// Finde die passende Engine für diesen Controller (z.B. TCP Client, TCP Server oder UDP)
			messageEngines.stream()
					.filter(engine -> engine.supports(controller))
					.findFirst()
					.ifPresent(engine -> processMessagesForController(controller, engine));
		}
	}
	
	private void processMessagesForController(Controller controller, MessageEngine engine) {
		try (Transaction transaction = Transaction.create()) {
			// SQL-Abfrage pro Controller (verhindert riesige IN-Klauseln und SQL-Injection Gefahr)
			String sql = "SELECT ID, CONTENT FROM TRANSFER_OUT WHERE CONTROLLER_ID = (SELECT ID FROM CONTROLLER WHERE NAME= '"+controller.getName()+"')";
			ResultSet rs = transaction.select(sql); // Nutze Prepared Statements!
			
			while (rs.next()) {
				int id = rs.getInt("ID");
				byte[] content = rs.getBytes("CONTENT");
				
				boolean success = engine.sendMessage(controller, content);
				Transfer status = success ? Transfer.OK : Transfer.FAILED;
				
				transaction.update("UPDATE TRANSFER_OUT SET PROCESSED = '"+status+"' WHERE ID ="+id);
			}
		} catch (Exception e) {
			log.error("Fehler beim Verarbeiten von Controller {}", controller.getName(), e);
		}
	}
}
