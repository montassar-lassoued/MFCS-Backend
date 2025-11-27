package com.pilot.persistence;


import org.springframework.context.ApplicationContext;
import xml.DatabaseConfig;

import javax.sql.DataSource;
import java.sql.SQLException;

public class Persistence {


    private ApplicationContext context;

    private DataSource dataSource;
    private String databaseName;
    private String type;
    private String host;
    private int port;
    private String username;
    private String password;

    public Persistence(DatabaseConfig database, ApplicationContext context) {
        setDatabaseName(database.getName());
        setType(database.getType());
        setHost(database.getHost());
        setPort(database.getPort());
        setUsername(database.getUsername());
        setPassword(database.getPassword());
        this.context = context;
    }

    public void initialize() {
        dataSource = context.getBean(PersistenceDataSource.class).getDataSource();
    }

    public boolean isConnected() {
        try {
            return dataSource != null && dataSource.getConnection() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void close() throws SQLException {
        if (dataSource != null) dataSource.getConnection().close();
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

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
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
