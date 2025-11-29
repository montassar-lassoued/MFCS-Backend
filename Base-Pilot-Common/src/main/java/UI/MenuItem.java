package UI;

public class MenuItem {
    private String name;
    private MenuShortcut shortcut;
    private String color;

    public MenuItem(String name, MenuShortcut shortcut, String color) {
        this.name = name;
        this.shortcut = shortcut;
        this.color =color;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return shortcut.getUrl();
    }
    public String getColor() {
        return color;
    }
}
