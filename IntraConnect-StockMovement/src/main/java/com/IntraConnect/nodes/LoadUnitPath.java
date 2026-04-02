package com.IntraConnect.nodes;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class LoadUnitPath {
	
	public static NextStep calculateNextStep(
			Map<String, Node> graph,
			String current,
			String target) {
		
		Node currentNode = graph.get(current);
		if (currentNode == null) {
			return new NextStep(null, null, null, "Unbekannter Standort");
		}
		Node targetNode = graph.get(target);
		if (targetNode == null) {
			return new NextStep(null, null, null, "Unbekannter Ziel");
		}
		
		String controller = currentNode.controller;
		
		Map<String, Integer> dist = new HashMap<>();
		Map<String, String> prev = new HashMap<>();
		
		PriorityQueue<NodeDistance> pq =
				new PriorityQueue<>(Comparator.comparingInt(n -> n.dist));
		
		for (String p : graph.keySet()) {
			dist.put(p, Integer.MAX_VALUE);
		}
		
		dist.put(current, 0);
		pq.add(new NodeDistance(current, 0));
		
		while (!pq.isEmpty()) {
			NodeDistance nd = pq.poll();
			
			if (nd.point.equals(target)) break;
			
			Node node = graph.get(nd.point);
			
			for (Edge e : node.edges) {
				if (e.blocked) continue;
				
				int newDist = dist.get(nd.point) + e.cost;
				if (newDist < dist.get(e.target)) {
					dist.put(e.target, newDist);
					prev.put(e.target, nd.point);
					pq.add(new NodeDistance(e.target, newDist));
				}
			}
		}
		
		return extractFirstStep(graph, prev, current, target, controller);
	}
	
	private static NextStep extractFirstStep(
			Map<String, Node> graph,
			Map<String, String> prev,
			String start,
			String target,
			String controller) {
		
		String step = target;
		String before = prev.get(step);
		
		while (before != null && !before.equals(start)) {
			step = before;
			before = prev.get(step);
		}
		
		if (before == null) {
			return new NextStep(null, null, null, "Kein Weg gefunden");
		}
		
		Node startNode = graph.get(start);
		
		for (Edge e : startNode.edges) {
			if (e.target.equals(step)) {
				return new NextStep(controller, step, e.direction, "");
			}
		}
		
		return new NextStep(null, null, null, "Richtung nicht gefunden");
	}
}
