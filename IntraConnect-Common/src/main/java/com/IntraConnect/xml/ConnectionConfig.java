package com.IntraConnect.xml;

public class ConnectionConfig {
	
    private String host;
    private int port;
    private int timeout;
	
	public ConnectionConfig(String host, int port, int timeout) {
		this.host = host;
		this.port = port;
		this.timeout = timeout;
	}
	
	// Getter & Setter
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
