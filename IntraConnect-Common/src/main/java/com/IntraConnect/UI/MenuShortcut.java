package com.IntraConnect.UI;

public enum MenuShortcut {
    HOME("/home"),
    COMPANY("/company"),
    SERVICES("/com/IntraConnect/services"),
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
