package com.IntraConnect.scheduler;

import com.IntraConnect._enum.Transfer;
import com.IntraConnect.async.client.AsyncClientEngine;
import com.IntraConnect.async.server.AsyncServerEngine;
import com.IntraConnect.queryExec.transaction.Transaction;
import com.IntraConnect.services.TCPService;
import com.IntraConnect.controller.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ControllerMessageScheduler {
	
	private static final Logger log = LoggerFactory.getLogger(ControllerMessageScheduler.class);
	
	private final AsyncServerEngine serverEngine;
	private final AsyncClientEngine clientEngine;
	private final TCPService tcpService;
	
	public ControllerMessageScheduler(AsyncServerEngine serverEngine,
									  AsyncClientEngine clientEngine,
									  TCPService tcpService) {
		this.serverEngine = serverEngine;
		this.clientEngine = clientEngine;
		this.tcpService = tcpService;
	}
	
	/**
	 * Läuft alle 2 Sekunden
	 */
	@Scheduled(fixedRate = 2000)
	public void sendScheduledMessages() {
		// 1️⃣ alle Controller mit Pending Messages
		List<String> clients = new ArrayList<>();
		tcpService.getControllers().stream()
				.filter(Controller::isActive)
				.map(Controller::getName)
				.forEach(clients::add);
		
		sendMessage(clients, false);
		
		List<String> servers = new ArrayList<>();
		tcpService.getControllers().stream()
				.filter(c -> !c.isActive())
				.map(Controller::getName)
				.forEach(servers::add);
		
		sendMessage(servers, true);
		
	}
	
	private void sendMessage(List<String> controllers, boolean server){
		if (controllers.isEmpty()){
			return;
		}
		try (Transaction transaction = Transaction.create()){
			String crls = controllers.stream()
					.map(c -> "'" + c.replace("'", "''") + "'")
					.collect(Collectors.joining(","));
			
			String sql = "SELECT TRANSFER_OUT.ID, " +
					"CONTROLLER.NAME, " +
					"TRANSFER_OUT.CONTENT " +
					"FROM TRANSFER_OUT LEFT JOIN CONTROLLER " +
					"ON (TRANSFER_OUT.CONTROLLER_ID = CONTROLLER.ID) " +
					"WHERE CONTROLLER.NAME IN ("+crls+")";
			
			ResultSet resultSet = transaction.select(sql);
		
			while (resultSet.next()) {
				
				int transferOut_Id = resultSet.getInt("ID");
				String controllerName = resultSet.getString("NAME");
				byte[] content = resultSet.getBytes("CONTENT");
				
				boolean ok;
				if(server){
					ok = serverEngine.sendMessage(controllerName, content);
				}
				else {
					ok = clientEngine.sendMessage(controllerName, content);
				}
				Transfer transfer;
				if(ok){
					transfer = Transfer.OK;
				}
				else {
					transfer = Transfer.FAILED;
				}
				
				transaction.update("UPDATE TRANSFER_OUT SET PROCESSED = '"+ transfer +"' WHERE ID = "+transferOut_Id);
				
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
