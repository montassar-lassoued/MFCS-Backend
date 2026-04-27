package com.IntraConnect.dataService;

import com.IntraConnect.listViews.ViewsType;

import java.util.List;

public class BrowserMenu{
    String id;
    String name;
    String color;
    int order;
    String menuTyp;
    ViewsType viewTyp;
    List<BrowserMenu> children;

    public BrowserMenu( String id,
                        String name,
                        String color,
                        int order,
                        String menuTyp,
                        ViewsType viewTyp,
                        List<BrowserMenu> children){

        setId(id);
        setName(name);
        setColor(color);
        setOrder(order);
        setMenuTyp(menuTyp);
        setViewTyp(viewTyp);
        setChildren(children);

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMenuTyp() {
        return menuTyp;
    }

    public void setMenuTyp(String menuTyp) {
        this.menuTyp = menuTyp;
    }

    public ViewsType getViewTyp() {
        return viewTyp;
    }

    public void setViewTyp(ViewsType viewTyp) {
        this.viewTyp = viewTyp;
    }

    public List<BrowserMenu> getChildren() {
        return children;
    }

    public void setChildren(List<BrowserMenu> children) {
        this.children = children;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
