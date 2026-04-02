package com.IntraConnect.xml;

import java.util.List;

public class Group {
	
    private final String id;
    private final String label;
    private final String color;
    private final int order;
    private final List<Item> items;
	
	public Group(String id, String label, String color, int order, List<Item> items) {
		this.id = id;
		this.label = label;
		this.color = color;
		this.order = order;
		this.items = items;
	}
	
	public String getId() { return id; }
    public String getLabel() { return label; }
    public List<Item> getItems() { return items; }
    public String getColor() {
        return color;
    }
    public int getOrder() {
        return order;
    }
}