package com.IntraConnect.scheduler;

import com.IntraConnect._enum.Transfer;
import com.IntraConnect.datagram.client.UdpClientEngine;
import com.IntraConnect.datagram.server.UdpServerEngine;
import com.IntraConnect.queryExec.transaction.Transaction;
import com.IntraConnect.services.UDPService;
import com.IntraConnect.controller.Controller;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UdpScheduler {
	
	private final UDPService updService;
	private final UdpServerEngine udpServerEngine;
	private final UdpClientEngine clientEngine;
	
	public UdpScheduler(UDPService updService,
						UdpServerEngine udpServerEngine,
						UdpClientEngine clientEngine) {
		
		this.updService = updService;
		this.udpServerEngine = udpServerEngine;
		this.clientEngine = clientEngine;
	}
	
	@Scheduled(fixedRate = 5000)
	public void sendScheduledMessages() {
		// 1️⃣ alle Controller mit Pending Messages
		List<Controller> clients = new ArrayList<>();
		updService.getControllers().stream()
				.filter(Controller::isActive)
				.forEach(clients::add);
		
		sendMessage(clients, false);
		
		List<Controller> servers = new ArrayList<>();
		updService.getControllers().stream()
				.filter(c -> !c.isActive())
				.forEach(clients::add);
		
		sendMessage(servers, true);
		
		
	}
	
	private void sendMessage(List<Controller> controllers, boolean server){
		if (controllers.isEmpty()){
			return;
		}
		try (Transaction transaction = Transaction.create()){
			String crls = controllers.stream()
					.map(c -> "'" + c.getName().replace("'", "''") + "'")
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
				
				Controller controller = controllers.stream()
						.filter(ctr-> ctr.getName().equals(controllerName))
						.findFirst()
						.orElseGet(null);
				boolean ok;
				if(server){
					ok = udpServerEngine.sendMessage(controller, content);
				}
				else {
					ok = clientEngine.sendMessage(controller, content);
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
	
	/*private void sendMessage(List<Controller> controllers, boolean server){
		
		ResultSet resultSet = dataService.getPayloadsByControllers(controllers.stream().map(Controller::getName).toList());
		try {
			while (resultSet.next()) {
				
				int transferOut_Id = resultSet.getInt("ID");
				String controllerName = resultSet.getString("NAME");
				byte[] content = resultSet.getBytes("CONTENT");
				
				Controller controller = controllers.stream()
						.filter(c-> c.getName().equals(controllerName))
						.findFirst()
						.orElseGet(null);
				boolean ok;
				if(server){
					ok = udpServerEngine.sendMessage(controller, content);
				}
				else {
					ok = clientEngine.sendMessage(controller, content);
				}
				Transfer transfer;
				if(ok){
					transfer = Transfer.OK;
				}
				else {
					transfer = Transfer.FAILED;
				}
				try(Transaction transaction = Transaction.create()){
					transaction.update("UPDATE TRANSFER_OUT SET PROCESSED = '"+ transfer +"' WHERE ID = "+transferOut_Id);
				}
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}*/
}
