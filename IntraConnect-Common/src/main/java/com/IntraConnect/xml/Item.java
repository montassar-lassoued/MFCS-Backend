package com.IntraConnect.xml;


public class Item {
	
    private final String id;
    private final String label;
    private final String color;
    private final int order;
    private final String navigateTo;
	
	public Item(String id, String label, String color, int order, String navigateTo) {
		this.id = id;
		this.label = label;
		this.color = color;
		this.order = order;
		this.navigateTo = navigateTo;
	}
	
	// getter & setter
    public String getId() { return id; }
    public String getLabel() { return label; }
    public String getNavigateTo() { return navigateTo; }
    public String getColor() {
        return color;
    }
    public int getOrder() {
        return order;
    }
}