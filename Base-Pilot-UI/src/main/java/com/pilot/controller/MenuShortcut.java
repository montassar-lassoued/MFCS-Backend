package com.pilot.controller;

public enum MenuShortcut {
    HOME("/home"),
    COMPANY("/company"),
    SERVICES("/services"),
    GALLERY("/gallery"),
    NEWS("/news"),
    CONTACT("/contact");

    private final String url;

    MenuShortcut(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
