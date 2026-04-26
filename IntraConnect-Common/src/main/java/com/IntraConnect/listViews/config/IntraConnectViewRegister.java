package com.IntraConnect.listViews.config;

import com.IntraConnect.intf.IntraConnectViewFactory;
import com.IntraConnect.listViews.IntraConnectView;

import java.util.ArrayList;
import java.util.List;


public class IntraConnectViewRegister {

    private final List<IntraConnectViewFactory> factories = new ArrayList<>();

    public void addFactory(IntraConnectViewFactory factory) {
        factories.add(factory);
    }

    public List<IntraConnectView> instantiateAll() {
        return factories.stream().map(IntraConnectViewFactory::create).toList();
    }

    public List<IntraConnectViewFactory> getFactories() {
        return factories;
    }
}