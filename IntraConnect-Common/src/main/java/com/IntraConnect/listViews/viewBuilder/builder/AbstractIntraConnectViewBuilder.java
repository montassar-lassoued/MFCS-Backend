package com.IntraConnect.listViews.viewBuilder.builder;

import com.IntraConnect.intf.IntraConnectServiceRequest;
import com.IntraConnect.listViews.Buttons;
import com.IntraConnect.listViews.ViewButton;
import com.IntraConnect.listViews.ViewsType;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractIntraConnectViewBuilder <T extends AbstractIntraConnectViewBuilder<T>>{
    protected final ViewsType type;
    protected String query;
    protected List<ViewButton> viewButtons = new ArrayList<>();

    protected AbstractIntraConnectViewBuilder(ViewsType type) {
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    public T query(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalStateException("Query can't be empty");
        }
        this.query = query.toUpperCase();
        return self();
    }

    public T addViewButton(Buttons button, String label, IntraConnectServiceRequest requestService) {
        viewButtons.add(new ViewButton(button,label, requestService));
        return self();
    }
	
	public abstract IntraConnectViewDetails build();
}
