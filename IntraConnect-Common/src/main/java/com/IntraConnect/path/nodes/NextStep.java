package com.IntraConnect.path.nodes;

public class NextStep {
	
	String controller;   // Controller vom aktuellen Punkt
	String nextTarget;
	String direction;
	String error;
	
	NextStep(String controller, String nextTarget, String direction, String error) {
		this.controller = controller;
		this.nextTarget = nextTarget;
		this.direction = direction;
		this.error = error;
	}
	
	public String getController() {
		return controller;
	}
	
	public String getNextTarget() {
		return nextTarget;
	}
	
	public String getDirection() {
		return direction;
	}
	
	public String getError() {
		return error;
	}
}
