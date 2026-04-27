package com.IntraConnect.xml;


public class Item {
	
    private final String id;
    private final String label;
    private final String color;
    private final int order;
	
	public Item(String id, String label, String color, int order) {
		this.id = id;
		this.label = label;
		this.color = color;
		this.order = order;
	}
	
	// getter & setter
    public String getId() { return id; }
    public String getLabel() { return label; }
    public String getColor() {
        return color;
    }
    public int getOrder() {
        return order;
    }
}