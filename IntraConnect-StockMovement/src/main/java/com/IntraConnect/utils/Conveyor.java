package com.IntraConnect.utils;

import com.IntraConnect.path.nodes.Edge;
import com.IntraConnect.path.nodes.Graph;
import com.IntraConnect.path.nodes.Node;

public class Conveyor {
	
	public static String getController(String location){
		Node startNode = Graph.get().get(location);
		return startNode.getController();
	}
	
	public static String getDirection(String location, String nextDestination){
		Node startNode = Graph.get().get(location);
		
		for (Edge e : startNode.getEdges()) {
			if (e.getTarget().equals(nextDestination)) {
				return e.getDirection();
			}
		}
		return null;
	}
	public static NodeStep getNextStep(String location, String nextDestination){
		Node startNode = Graph.get().get(location);
		
		for (Edge e : startNode.getEdges()) {
			if (e.getTarget().equals(nextDestination)) {
				return new NodeStep(startNode.getController(), e.getDirection());
			}
		}
		return null;
	}
}
