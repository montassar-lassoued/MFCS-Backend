package com.IntraConnect.listViews.config;

import com.IntraConnect.intf.PilotViewFactory;
import com.IntraConnect.listViews.PilotView;

import java.util.ArrayList;
import java.util.List;


public class PilotViewRegister {

    private final List<PilotViewFactory> factories = new ArrayList<>();

    public void addFactory(PilotViewFactory factory) {
        factories.add(factory);
    }

    public List<PilotView> instantiateAll() {
        return factories.stream().map(PilotViewFactory::create).toList();
    }

    public List<PilotViewFactory> getFactories() {
        return factories;
    }
}