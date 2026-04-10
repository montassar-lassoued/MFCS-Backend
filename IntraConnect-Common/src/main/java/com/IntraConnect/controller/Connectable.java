package com.IntraConnect.controller;


import com.IntraConnect.xml.ControllerConfig;

import java.util.HashMap;
import java.util.Map;

public class Connectable {

    private String name;
    private boolean active;
    private String prefix;
    private String suffix;
    private String host;
    private int port;
    private int timeout;
	// Flexibler Speicher für protokollspezifische Einstellungen
	private final Map<String, Object> properties = new HashMap<>();

    public Connectable(ControllerConfig config){

        setName(config.getName());
        setActive(config.isActive());
        setPrefix(config.getPrefix());
        setSuffix(config.getSuffix());
        setHost(config.getConnection().getHost());
        setPort(config.getConnection().getPort());
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        if(name.isBlank()){
            throw new RuntimeException("Controller-name is missing");
        }
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        if(host.isBlank() && !isActive()){
            throw new RuntimeException("Controller-IP ist missing");
        }
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if(String.valueOf(port).isBlank()){
            throw new RuntimeException("Controller-Port is missing");
        }
        this.port = port;
    }
	
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}
	
	public Object getProperty(String key) {
		return properties.get(key);
	}
}
