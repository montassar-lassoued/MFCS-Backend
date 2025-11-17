package com.pilot.controller;


import xml.ControllerConfig;

public class Controller {

    private String name;
    private boolean active;
    private String host;
    private int port;
    private int timeout;


    public Controller(ControllerConfig config){

        setName(config.getName());
        setActive(config.isActive());
        setHost(config.getConnection().getHost());
        setPort(config.getConnection().getPort());
        setTimeout(config.getConnection().getTimeout());
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

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
