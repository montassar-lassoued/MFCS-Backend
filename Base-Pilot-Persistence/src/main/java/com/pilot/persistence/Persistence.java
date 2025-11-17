package com.pilot.persistence;


import com.zaxxer.hikari.HikariDataSource;
import xml.DatabaseConfig;

public class Persistence {

    private HikariDataSource dataSource;
    private String type;
    private String host;
    private int port;
    private String username;
    private String password;

    public Persistence(DatabaseConfig database) {
        setType(database.getType());
        setHost(database.getHost());
        setPort(database.getPort());
        setUsername(database.getUsername());
        setPassword(database.getPassword());
    }

    public void initialize() {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(getHost());
        dataSource.setUsername(getUsername());
        dataSource.setPassword(getPassword());
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    }

    public boolean isConnected() {
        try {
            return dataSource != null && dataSource.getConnection() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public void close() {
        if (dataSource != null) dataSource.close();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if(type.isBlank()){
            throw new RuntimeException("Database-Typ is missing");
        }
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        if(host.isBlank()){
            throw new RuntimeException("Database-Host is missing");
        }
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if(String.valueOf(port).isBlank()){
            throw new RuntimeException("Database-Port is missing");
        }
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if(username.isBlank()){
            throw new RuntimeException("Database-Username is missing");
        }
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
