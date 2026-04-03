package com.IntraConnect.messageEngine;

import com.IntraConnect.controller.Controller;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ControllerRegistry {
	// Speichert alle Controller systemweit, egal aus welchem Modul
	private final List<Controller> allControllers = new CopyOnWriteArrayList<>();
	
	public void register(List<Controller> controllers) {
		this.allControllers.addAll(controllers);
	}
	
	public List<Controller> getAllControllers() {
		return allControllers;
	}
}
