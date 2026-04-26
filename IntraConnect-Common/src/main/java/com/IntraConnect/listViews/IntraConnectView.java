package com.IntraConnect.listViews;

import com.IntraConnect.UI.MenuItem;
import com.IntraConnect.intf.IntraConnectUI;
import com.IntraConnect.intf.IntraConnectViewFactory;

import java.util.List;


public abstract class IntraConnectView implements IntraConnectUI {

    public List<MenuItem> getViews() {
        return listViews();

    }
	
    public abstract IntraConnectViewFactory getClassType();
}
