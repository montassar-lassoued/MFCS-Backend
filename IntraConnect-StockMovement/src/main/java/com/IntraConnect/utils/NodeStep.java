package com.IntraConnect.utils;

public class NodeStep {
	
	String controller;   // Controller vom aktuellen Punkt
	String direction;
	
	NodeStep(String controller, String direction) {
		this.controller = controller;
		this.direction = direction;
	}
	
	public String getController() {
		return controller;
	}
	
	public String getDirection() {
		return direction;
	}
	
}
