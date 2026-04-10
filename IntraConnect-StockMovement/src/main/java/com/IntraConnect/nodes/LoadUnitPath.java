package com.IntraConnect.nodes;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Definition der Klasse zur Berechnung des Pfades für eine Ladeeinheit.
 */
public class LoadUnitPath {
	
	/**
	 * Berechnet den nächsten Navigationsschritt basierend auf dem aktuellen Standort und dem Ziel.
	 */
	public static NextStep calculateNextStep(
			Map<String, Node> graph, // Die Karte aller Knoten im System (Graph-Struktur).
			String current,          // Die ID des aktuellen Standorts der Ladeeinheit.
			String target) {         // Die ID des gewünschten Zielknotens.
		
		// Ruft das Knoten-Objekt für den aktuellen Standort aus der Map ab.
		Node currentNode = graph.get(current);
		// Prüft, ob der aktuelle Standort überhaupt im Graphen existiert.
		if (currentNode == null) {
			// Gibt eine Fehlermeldung zurück, wenn der Startpunkt unbekannt ist.
			return new NextStep(null, null, null, "Unbekannter Standort");
		}
		// Ruft das Knoten-Objekt für das Ziel aus der Map ab.
		Node targetNode = graph.get(target);
		// Prüft, ob das Ziel im Graphen existiert.
		if (targetNode == null) {
			// Gibt eine Fehlermeldung zurück, wenn das Ziel nicht existiert.
			return new NextStep(null, null, null, "Unbekannter Ziel");
		}
		
		// Speichert die Kennung des Controllers, der für den aktuellen Knoten zuständig ist.
		String controller = currentNode.controller;
		
		// Map zur Speicherung der bisher kürzesten Distanz vom Start zu jedem Knoten.
		Map<String, Integer> dist = new HashMap<>();
		// Map zur Speicherung des Vorgängers eines Knotens (um den Pfad zurückzuverfolgen).
		Map<String, String> prev = new HashMap<>();
		
		// Prioritätswarteschlange, um immer den Knoten mit der geringsten Distanz zuerst zu bearbeiten.
		PriorityQueue<NodeDistance> pq =
				new PriorityQueue<>(Comparator.comparingInt(n -> n.dist));
		
		// Initialisiert alle Distanzen im Graphen mit dem maximal möglichen Wert (Unendlich).
		for (String p : graph.keySet()) {
			dist.put(p, Integer.MAX_VALUE);
		}
		
		// Setzt die Distanz für den Startknoten auf 0.
		dist.put(current, 0);
		// Fügt den Startknoten als ersten Prüfpunkt in die Prioritätswarteschlange ein.
		pq.add(new NodeDistance(current, 0));
		
		// Startet die Hauptschleife des Dijkstra-Algorithmus (solange noch Knoten zu prüfen sind).
		while (!pq.isEmpty()) {
			// Entnimmt den Knoten mit der aktuell kleinsten Distanz aus der Warteschlange.
			NodeDistance nd = pq.poll();
			
			// Wenn das Ziel erreicht wurde, kann die Suche vorzeitig abgebrochen werden.
			if (nd.point.equals(target)) break;
			
			// Holt die Knotendaten (Kanten/Verbindungen) für den aktuell bearbeiteten Punkt.
			Node node = graph.get(nd.point);
			
			// Durchläuft alle ausgehenden Verbindungen (Edges) des aktuellen Knotens.
			for (Edge e : node.edges) {
				// Überspringt die Verbindung, falls sie aktuell blockiert ist (z.B. durch Wartung).
				if (e.blocked) continue;
				
				// Berechnet die potenzielle neue Distanz zum Zielknoten dieser Kante.
				int newDist = dist.get(nd.point) + e.cost;
				// Prüft, ob dieser neue Weg kürzer ist als der bisher bekannte Weg zu diesem Knoten.
				if (newDist < dist.get(e.target)) {
					// Aktualisiert die kürzeste Distanz für den Zielknoten der Kante.
					dist.put(e.target, newDist);
					// Speichert den aktuellen Knoten als optimalen Vorgänger für den Zielknoten.
					prev.put(e.target, nd.point);
					// Fügt den Zielknoten mit der neuen Distanz zur weiteren Bearbeitung in die Warteschlange ein.
					pq.add(new NodeDistance(e.target, newDist));
				}
			}
		}
		
		// Übergibt die Pfadhistorie an die Hilfsmethode, um den allerersten Schritt zu bestimmen.
		return extractFirstStep(graph, prev, current, target, controller);
	}
	
	/**
	 * Hilfsmethode, die den berechneten Gesamtpfad rückwärts durchläuft, um den nächsten Einzelschritt zu finden.
	 */
	private static NextStep extractFirstStep(
			Map<String, Node> graph, // Der gesamte Graph.
			Map<String, String> prev, // Die berechneten Vorgänger-Beziehungen.
			String start,            // Der Ausgangspunkt.
			String target,           // Das Endziel.
			String controller) {     // Der zuständige Controller.
		
		// Beginnt die Rückverfolgung beim Zielknoten.
		String step = target;
		// Holt den Vorgänger des Zielknotens.
		String before = prev.get(step);
		
		// Läuft den Pfad rückwärts ab, bis der Knoten direkt nach dem Startpunkt erreicht ist.
		while (before != null && !before.equals(start)) {
			// Der aktuelle Knoten wird zum nächsten Ziel in der Rückwärtskette.
			step = before;
			// Der Vorgänger dieses Knotens wird abgerufen.
			before = prev.get(step);
		}
		
		// Falls kein Vorgänger gefunden wurde, existiert keine Verbindung zwischen Start und Ziel.
		if (before == null) {
			// Rückgabe einer entsprechenden Fehlermeldung.
			return new NextStep(null, null, null, "Kein Weg gefunden");
		}
		
		// Holt die Daten des Startknotens, um die physikalische Richtung zu bestimmen.
		Node startNode = graph.get(start);
		
		// Sucht in den ausgehenden Kanten des Startknotens nach der Verbindung zum ermittelten nächsten Schritt.
		for (Edge e : startNode.edges) {
			// Wenn die Kante zum berechneten nächsten Knoten führt:
			if (e.target.equals(step)) {
				// Gibt den finalen nächsten Schritt inklusive Controller und Fahrtrichtung zurück.
				return new NextStep(controller, step, e.direction, "");
			}
		}
		
		// Falls der Pfad logisch existiert, aber keine passende Kante gefunden wurde (Dateninkonsistenz).
		return new NextStep(null, null, null, "Richtung nicht gefunden");
	}
}
