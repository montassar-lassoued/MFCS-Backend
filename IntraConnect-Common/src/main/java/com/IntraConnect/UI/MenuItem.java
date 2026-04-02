package com.IntraConnect.UI;

import com.IntraConnect.listViews.viewBuilder.PilotViewDetails;

public class MenuItem {
    private final String name;
    private final PilotViewDetails view;


    public MenuItem(String name, PilotViewDetails view) {
        this.name = name;
        this.view = view;
    }

    public String getName() {
        return name;
    }
    public PilotViewDetails getView(){return this.view;}
}
