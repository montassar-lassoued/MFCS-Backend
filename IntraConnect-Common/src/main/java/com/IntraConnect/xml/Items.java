package com.IntraConnect.xml;

import java.util.List;

public class Items {
	
    private List<Item> items;
    private List<Group> groups;
	
	public Items(List<Item> items, List<Group> groups) {
		this.items = items;
		this.groups = groups;
	}
	
	public List<Item> getItems() { return items; }
    public List<Group> getGroups() { return groups; }
}