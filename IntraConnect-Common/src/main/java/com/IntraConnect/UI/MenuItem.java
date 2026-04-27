package com.IntraConnect.UI;

import com.IntraConnect.listViews.viewBuilder.builder.IntraConnectViewDetails;

public class MenuItem {
	private final String id;
    private final IntraConnectViewDetails view;


    public MenuItem(String id, IntraConnectViewDetails view) {
        this.view = view;
		this.id = id;
    }
	
	public String getId() {
		return id;
	}
	
    public IntraConnectViewDetails getView(){return this.view;}
}
