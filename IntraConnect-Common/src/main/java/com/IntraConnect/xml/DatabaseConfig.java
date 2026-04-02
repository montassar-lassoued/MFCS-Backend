package com.IntraConnect.xml;


public class DatabaseConfig {
	public void setType(String type) {
		this.type = type;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setEncrypt(boolean encrypt) {
		this.encrypt = encrypt;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	private String type;
    private String host;
    private int port;
    private String name;
    private boolean encrypt;
    private String username;
    private String password;

    // Getter & Setter
    public String getType() {
        return type;
    }

    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public boolean getEncrypt() {
        return encrypt;
    }
}
