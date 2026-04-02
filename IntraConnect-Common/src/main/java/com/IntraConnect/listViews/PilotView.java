package com.IntraConnect.listViews;

import com.IntraConnect.UI.MenuItem;
import com.IntraConnect.intf.PilotUI;
import com.IntraConnect.intf.PilotViewFactory;

import java.util.List;


public abstract class PilotView implements PilotUI {

    public List<MenuItem> getViews() {
        return listViews();

    }
	
    public abstract PilotViewFactory getClassType();
}
