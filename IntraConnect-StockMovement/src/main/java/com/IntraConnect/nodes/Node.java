package com.IntraConnect.nodes;

import java.util.ArrayList;
import java.util.List;

public class Node {
	String point;
	String controller;
	List<Edge> edges = new ArrayList<>();
	
	public Node(String point, String controller) {
		this.point = point;
		this.controller = controller;
	}
	
	public String getPoint() {
		return point;
	}
	
	public String getController() {
		return controller;
	}
	
	public List<Edge> getEdges() {
		return edges;
	}
	
	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}
}
