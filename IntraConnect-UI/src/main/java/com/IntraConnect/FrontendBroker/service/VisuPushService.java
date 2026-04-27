package com.IntraConnect.FrontendBroker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class VisuPushService {
	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	
	public void publishLuMovement(String luId, String station, String type, String direction) {
		Map<String, String> payload = new HashMap<>();
		payload.put("type", type); // 'LEFT' oder 'ARRIVED'
		payload.put("stationName", station);
		payload.put("luId", luId);
		payload.put("direction", direction);
		
		messagingTemplate.convertAndSend("/topic/events", payload);
	}
}
