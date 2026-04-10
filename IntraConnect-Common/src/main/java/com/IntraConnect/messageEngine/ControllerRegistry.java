package com.IntraConnect.messageEngine;

import com.IntraConnect.controller.Connectable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ControllerRegistry {
	// Speichert alle Controller systemweit, egal aus welchem Modul
	private final List<Connectable> allConnectables = new CopyOnWriteArrayList<>();
	
	public void register(List<Connectable> connectables) {
		this.allConnectables.addAll(connectables);
	}
	
	public List<Connectable> getAllControllers() {
		return allConnectables;
	}
}
