package com.IntraConnect.xml;

import java.util.List;

public class ModuleConfig {
	
    private String name;
	
    private boolean enabled;
	
    private DatabaseConfig database;
	
    private List<ControllerConfig> controllers;
    private Navbar navigationBar;

    // Getter & Setter
    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public boolean isEnabled() { return enabled; }

    public DatabaseConfig getDatabase() { return database; }

    public List<ControllerConfig> getControllers() { return controllers; }

    public Navbar getNavigationBar() {
        return navigationBar;
    }
}