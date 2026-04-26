package com.IntraConnect.path.nodes;

public class Edge {
	String target;
	int cost;
	String direction;
	boolean blocked = false;
	
	public Edge(String target, String direction, int cost) {
		this.target = target;
		this.direction = direction;
		this.cost = cost;
	}
	
	public String getTarget() {
		return target;
	}
	
	public boolean isBlocked() {
		return blocked;
	}
	
	public String getDirection() {
		return direction;
	}
	
	public int getCost() {
		return cost;
	}
}
