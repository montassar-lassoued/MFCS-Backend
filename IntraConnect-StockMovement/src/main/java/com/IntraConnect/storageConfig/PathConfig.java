package com.IntraConnect.storageConfig;

import com.IntraConnect._enum.Systemtype;
import com.IntraConnect.path.nodes.Edge;
import com.IntraConnect.path.nodes.Graph;
import com.IntraConnect.path.nodes.Node;
import com.IntraConnect.queryExec.transaction.Transaction;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathConfig {
	
	Map<String, Node> graph = new HashMap<>();
	
	public void load(Element elNodes){
		List<Element> nodes = elNodes.getChildren("Node");
		for (Element point : nodes) {
			
			//***** Attribute
			String name = point.getAttributeValue("point");
			String controller = point.getAttributeValue("connectable");
			
			Node node = new Node(name, controller);
			
			//******* Ziele
			List<Edge> edges = new ArrayList<>();
			List<Element> targets = point.getChildren("Ziel");
			for (Element target: targets){
				
				String tName = target.getAttributeValue("point");
				String tDirection = target.getAttributeValue("direction");
				int tCost = Integer.parseInt(target.getAttributeValue("cost", "100"));
				
				edges.add(new Edge(tName,tDirection, tCost));
			}
			node.setEdges(edges);
			
			if(graph.containsKey(node.getPoint())){
				throw new RuntimeException("Duplikate vorhanden:"+node.getPoint());
			}
			graph.put(node.getPoint(), node);
		}
	}
	
	public void validate(){
		for (Node node : graph.values()) {
			// 1. Check: Existieren alle Ziele?
			for (Edge edge : node.getEdges()) {
				if (!graph.containsKey(edge.getTarget())) {
					throw new RuntimeException(
							"Validierungsfehler: Knoten '" + node.getPoint() +
									"' verweist auf unbekanntes Ziel '" + edge.getTarget() + "'"
					);
				}
				
				// 2. Check: Kosten valide?
				if (edge.getCost() < 0) {
					throw new RuntimeException(
							"Validierungsfehler: Negative Kosten bei Verbindung von " +
									node.getPoint() + " nach " + edge.getTarget()
					);
				}
				// 2. Check: Direction valide?
				if (edge.getDirection().isBlank()) {
					throw new RuntimeException(
							"Validierungsfehler: keine Richtung vorhanden bei Verbindung von " +
									node.getPoint() + " nach " + edge.getTarget()
					);
				}
			}
			
			// 3. Check: Sackgassen (falls nicht erlaubt) //*** wird momentan nicht gebraucht
			/*if (node.getEdges().isEmpty()) {
				throw new RuntimeException("Warnung: Knoten '" + node.getPoint() + "' hat keine ausgehenden Verbindungen.");
			}*/
		}
	}
	
	public void register(){
		List<Object[]> nodes = new ArrayList<>();
		for (Node node : graph.values()) {
			nodes.add(new  Object[]{node.getPoint()});
		}
		try (Transaction transaction = Transaction.create()){

				String sql = "INSERT INTO POINTS (NAME, TYPE, BLOCKED) VALUES (?, '" + Systemtype.CONVEYOR.name() + "', 0)";
				transaction.insertBatch(sql, nodes);
				
				transaction.commit();
			
			Graph.register(graph);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
