package com.IntraConnect.xml;

public class ControllerConfig {
	
    private String name;
    private boolean active;
    private String prefix;
    private String suffix;
    private ConnectionConfig connection;
	
	public ControllerConfig(String name, boolean active, String prefix, String suffix, ConnectionConfig connection) {
		this.name = name;
		this.active = active;
		this.prefix = prefix;
		this.suffix = suffix;
		this.connection = connection;
	}
	
	// Getter & Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ConnectionConfig getConnection() {
        return connection;
    }

    public void setConnection(ConnectionConfig connection) {
        this.connection = connection;
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
}

