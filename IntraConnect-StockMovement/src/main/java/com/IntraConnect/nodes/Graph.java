package com.IntraConnect.nodes;

import java.util.HashMap;
import java.util.Map;

public class Graph {
	
	static Map<String, Node> graph = new HashMap<>();
	
	public static void register(Map<String, Node> graph){
	
		Graph.graph = graph;
	}
	public static Map<String, Node> get(){
		return graph;
	}
}
