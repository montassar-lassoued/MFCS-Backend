package com.IntraConnect.listViews;

import com.IntraConnect.listViews.viewBuilder.View;

public class UIButton {

    String id;
    String label;
    String icon;
    View view;
    RequestMode requestMode;
    public UIButton(String id, String label, String icon, View view, RequestMode requestMode){
        setId(id);
        setLabel(label);
        setIcon(icon);
        setActionType(view);
        setRequestMode(requestMode);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public View getActionType() {
        return view;
    }

    public void setActionType(View view) {
        this.view = view;
    }

    public RequestMode getRequestMode() {
        return requestMode;
    }

    public void setRequestMode(RequestMode requestMode) {
        this.requestMode = requestMode;
    }
}
