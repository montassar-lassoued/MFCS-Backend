package com.IntraConnect.services;

import com.IntraConnect.UI.MenuItem;
import com.IntraConnect.dataService.BrowserMenu;
import com.IntraConnect.command.handlerReg.Register;
import com.IntraConnect.intf.PilotApplicationServices;
import com.IntraConnect.intf.PilotViewFactory;
import com.IntraConnect.listViews.PilotView;
import com.IntraConnect.listViews.config.PilotViewRegister;
import com.IntraConnect.listViews.config.ViewConfig;
import com.IntraConnect.listViews.ViewsType;
import com.IntraConnect.xml.Group;
import com.IntraConnect.xml.Item;
import com.IntraConnect.xml.Items;
import com.IntraConnect.xml.Navbar;
import org.jdom2.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class UIService extends PilotApplicationServices {

    List<MenuItem> viewItems = new ArrayList<>();
    List<BrowserMenu> browserMenus = new ArrayList<>();
    private PilotViewRegister viewRegister;
	boolean enabled;
	
	public UIService(Register register) {
		super(register);
	}
	
	@Override
    public String getName() {
        return "UI";
    }
	
	@Override
	public void configuration(Element module, ApplicationContext context) {
		if(module != null) {
			
			enabled = Boolean.parseBoolean(module.getAttributeValue("enabled"));
			if (!enabled){
				return;
			}
			List<Group> navbar_groups = new ArrayList<>();
			List<Item> _items = new ArrayList<>();
			
			Element navBar = module.getChild("Navbar");
			Element navBar_items = navBar.getChild("Items");
			List<Element> items = navBar_items.getChildren("Item");
			for (Element item : items){
				String id = item.getAttributeValue("id");
				String label = item.getAttributeValue("label");
				String navigateTo = item.getAttributeValue("navigateTo");
				String color = item.getAttributeValue("color");
				int order = Integer.parseInt(item.getAttributeValue("order"));
				
				Item _item = new Item(id, label, color, order, navigateTo);
				_items.add(_item);
			}
			
			List<Element> groups = navBar_items.getChildren("Group");
			for (Element grp : groups){
				
				String g_id = grp.getAttributeValue("id");
				String g_label = grp.getAttributeValue("label");
				String g_color = grp.getAttributeValue("color");
				int g_order = Integer.parseInt(grp.getAttributeValue("order"));
				
				List<Item> _gItems = new ArrayList<>();
				List<Element> gItems = grp.getChildren("Item");
				for (Element item : gItems){
					String id = item.getAttributeValue("id");
					String label = item.getAttributeValue("label");
					String navigateTo = item.getAttributeValue("navigateTo");
					String color = item.getAttributeValue("color");
					int order = Integer.parseInt(item.getAttributeValue("order"));
					
					Item _item = new Item(id, label, color, order, navigateTo);
					_gItems.add(_item);
				}
				Group group = new Group(g_id, g_label, g_color, g_order, _gItems);
				navbar_groups.add(group);
			}
			
			Navbar navbar = new Navbar(new Items(_items, navbar_groups));
			//**
			buildBrowserMenu(navbar);
		}
	}
	
	@Override
	public void register() {
	
	}
	
	public void buildBrowserMenu(Navbar navigationBar) {

        if(navigationBar != null){
            navigationBar.getItems().getItems().forEach(i->{
                BrowserMenu bm = new BrowserMenu(
                        i.getId(),
                        i.getLabel(),
                        i.getNavigateTo(),
                        i.getColor(),
                        i.getOrder(),
                        "item",
                        null,
                        null
                );
                browserMenus.add(bm);
            });
            navigationBar.getItems().getGroups().forEach(group->{
                List<BrowserMenu> childrenItems =
                group.getItems().stream().map(
                        i -> new BrowserMenu(
                            i.getId(),
                            i.getLabel(),
                            i.getNavigateTo(),
                            i.getColor(),
                                i.getOrder(),
                            "item",
                            null,
                            null
                    )).collect(Collectors.toList());

                BrowserMenu groupMb = new BrowserMenu(
                        group.getId(),
                        group.getLabel(),
                        null,
                        group.getColor(),
                        group.getOrder(),
                        "group",
                        null,
                        childrenItems
                );

                browserMenus.add(groupMb);
            });
        }
        // views laden
        viewRegister = ViewConfig.createRegister();
    }


    @Override
    public void validate() {
		if (!enabled){
			return;
		}
        // views prüfen /vergleichen (xml-Config, Code), ob alles zusammen passt
        // wir Laden alle Views (com.IntraConnect.views)
        for (PilotViewFactory factory : viewRegister.getFactories()) {
            PilotView instance = factory.create();
            viewItems.addAll(instance.getViews());
        }

        Set<String> menuNames = viewItems.stream()
                .map(MenuItem::getName)
                .collect(Collectors.toSet());

        for (BrowserMenu map : browserMenus) {
            if(map.getMenuTyp().equals("group")){
                continue;
            }
            String label = map.getName();
            boolean exists = menuNames.contains(label);
            if(!exists){
                throw new RuntimeException("Kein View mit der Name "+label+" gefunden");
            }
        }

        for (String itemName: menuNames){
            boolean exists = existsInMenuItems(browserMenus, itemName, viewItems);
            if(!exists){
                throw new RuntimeException("Kein View mit der Name "+itemName+" gefunden");
            }
        }

        // Wenn alles ok ist -> build Browser-Menüs
        buildOrderBrowserMenus();
    }

    private void buildOrderBrowserMenus() {
        // First, sort children for group menus
        browserMenus.forEach(menu -> {
            if ("group".equals(menu.getMenuTyp()) && menu.getChildren() != null) {
                menu.getChildren().sort(Comparator.comparingLong(BrowserMenu::getOrder));
            }
        });
        // Then, sort the top-level menus
        browserMenus.sort(Comparator.comparingLong(BrowserMenu::getOrder));
    }

    public boolean existsInMenuItems(List<BrowserMenu> menuItems, String nameToCheck, List<MenuItem> items) {
        for (BrowserMenu item : menuItems) {
            if (item == null) continue;

            String type = item.getMenuTyp();
            if ("item".equals(type)) {
                if (nameToCheck.equals(item.getName())) {
                    // Addiere View Typ
                    ViewsType viewsType = items.stream().filter(i->i.getName().equals(nameToCheck)).findFirst().get().getView().getType();
                    item.setViewTyp(viewsType);
                    return true;
                }
            } else if ("group".equals(type)) {
                // Prüfe rekursiv die Kinder
                List<BrowserMenu> children = item.getChildren();
                if (children != null && existsInMenuItems(children, nameToCheck, items)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void run() {
		if (!enabled){
			return;
		}
    }

    @Override
    public void stop() {
		if (!enabled){
			return;
		}
    }

    @Bean
    public  List< BrowserMenu> BrowserMenus() {
        return browserMenus;
    }
    @Bean
    public  List<MenuItem> viewItems() {
        return viewItems;
    }

}
