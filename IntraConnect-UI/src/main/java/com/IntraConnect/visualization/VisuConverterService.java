package com.IntraConnect.visualization;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class VisuConverterService {
	
	public VisuData convertXmlToJson(String xmlString) throws Exception {
		Document doc = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder()
				.parse(new ByteArrayInputStream(xmlString.getBytes()));
		
		VisuData data = new VisuData();
		data.rects = parseRects(doc.getElementsByTagName("Rect"));
		data.arrows = parseArrows(doc.getElementsByTagName("Arrow"));
		data.aisles = parseAisles(doc.getElementsByTagName("Aisle"));
		
		return data;
	}
	
	private List<AisleDto> parseAisles(NodeList aisle) {
		List<AisleDto> aisleDtos = new ArrayList<>();
		for (int i = 0; i < aisle.getLength(); i++) {
			Element el = (Element) aisle.item(i);
			AisleDto a = new AisleDto();
			a.id = el.getAttribute("id");
			a.name = el.getAttribute("name");
			a.x = Double.parseDouble(el.getAttribute("x"));
			a.y = Double.parseDouble(el.getAttribute("y"));
			a.width = Double.parseDouble(el.getAttribute("width"));
			a.height = Double.parseDouble(el.getAttribute("height"));
			a.orientation = el.getAttribute("orientation");
			
			a.rbg = parseRbgs(el.getElementsByTagName("RBG"));
			
			aisleDtos.add(a);
		}
		return aisleDtos;

	}
	
	private Rbg parseRbgs(NodeList rbg) {
		Rbg r = new Rbg();
		for (int i = 0; i < rbg.getLength(); i++) {
			Element el = (Element) rbg.item(i);
			r.id = el.getAttribute("id");
			r.name = el.getAttribute("name");
			r.positionOffset = Double.parseDouble(el.getAttribute("positionOffset"));
			r.width = Double.parseDouble(el.getAttribute("width"));
			r.height = Double.parseDouble(el.getAttribute("height"));
		}
		return r;
	}
	
	private List<RectDto> parseRects(NodeList list) {
		List<RectDto> rects = new ArrayList<>();
		for (int i = 0; i < list.getLength(); i++) {
			Element el = (Element) list.item(i);
			RectDto r = new RectDto();
			r.id = el.getAttribute("id");
			r.name = el.getAttribute("name");
			r.x = Double.parseDouble(el.getAttribute("x"));
			r.y = Double.parseDouble(el.getAttribute("y"));
			r.width = Double.parseDouble(el.getAttribute("width"));
			r.height = Double.parseDouble(el.getAttribute("height"));
			r.controller = el.getAttribute("controller");
			rects.add(r);
		}
		return rects;
	}
	
	private List<ArrowDto> parseArrows(NodeList list) {
		List<ArrowDto> arrows = new ArrayList<>();
		for (int i = 0; i < list.getLength(); i++) {
			Element el = (Element) list.item(i);
			ArrowDto a = new ArrowDto();
			a.id = el.getAttribute("id");
			a.fromRectId = el.getAttribute("from");
			a.toRectId = el.getAttribute("to");
			a.direction = el.getAttribute("direction");
			a.speed = Double.parseDouble(el.getAttribute("speed").isEmpty() ? "5" : el.getAttribute("speed"));
			
			// Waypoint Parsing: "383,203;304,203" -> List<Point>
			a.waypoints = parseWaypoints(el.getAttribute("waypoints"));
			
			arrows.add(a);
		}
		return arrows;
	}
	
	private List<Point> parseWaypoints(String wpString) {
		List<Point> points = new ArrayList<>();
		if (wpString == null || wpString.trim().isEmpty()) return points;
		
		String[] pairs = wpString.split(";");
		for (String pair : pairs) {
			String[] coords = pair.split(",");
			if (coords.length == 2) {
				points.add(new Point(
						Double.parseDouble(coords[0]),
						Double.parseDouble(coords[1])
				));
			}
		}
		return points;
	}
}
