package com.IntraConnect.services;

import com.IntraConnect.UI.MenuItem;
import com.IntraConnect.dataService.BrowserMenu;
import com.IntraConnect.command.handlerReg.Register;
import com.IntraConnect.intf.IntraConnectApplicationServices;
import com.IntraConnect.intf.IntraConnectViewFactory;
import com.IntraConnect.listViews.IntraConnectView;
import com.IntraConnect.listViews.config.IntraConnectViewRegister;
import com.IntraConnect.listViews.config.ViewConfig;
import com.IntraConnect.listViews.ViewsType;
import com.IntraConnect.xml.Group;
import com.IntraConnect.xml.Item;
import com.IntraConnect.xml.Items;
import com.IntraConnect.xml.Navbar;
import org.jdom2.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UIService extends IntraConnectApplicationServices {
	
	private final List<MenuItem> viewItems = new ArrayList<>();
	private final List<BrowserMenu> browserMenus = new ArrayList<>();
	private IntraConnectViewRegister viewRegister;
	private boolean enabled;
	
	public UIService(Register register) {
		super(register);
	}
	
	@Override
	public String getName() {
		return "UI";
	}
	
	@Override
	public void configuration(Element module, ApplicationContext context) {
		if (module == null) return;
		
		this.enabled = Boolean.parseBoolean(module.getAttributeValue("enabled"));
		if (!enabled) return;
		
		Element navBar = module.getChild("Navbar");
		if (navBar == null) return;
		
		Element navBarItems = navBar.getChild("Items");
		if (navBarItems == null) return;
		
		// 1. Top-Level Items parsen
		List<Item> directItems = navBarItems.getChildren("Item").stream()
				.map(this::parseItemElement)
				.collect(Collectors.toCollection(ArrayList::new));
		
		// 2. Gruppen parsen
		List<Group> groups = navBarItems.getChildren("Group").stream()
				.map(grp -> {
					List<Item> groupItems = grp.getChildren("Item").stream()
							.map(this::parseItemElement)
							.collect(Collectors.toCollection(ArrayList::new));
					return new Group(
							grp.getAttributeValue("id"),
							grp.getAttributeValue("label"),
							grp.getAttributeValue("color"),
							Integer.parseInt(grp.getAttributeValue("order", "0")),
							groupItems
					);
				})
				.collect(Collectors.toCollection(ArrayList::new));
		
		buildBrowserMenu(new Navbar(new Items(directItems, groups)));
	}
	
	private Item parseItemElement(Element el) {
		return new Item(
				el.getAttributeValue("id"),
				el.getAttributeValue("label"),
				el.getAttributeValue("color"),
				Integer.parseInt(el.getAttributeValue("order", "0"))
		);
	}
	
	public void buildBrowserMenu(Navbar navigationBar) {
		if (navigationBar == null) return;
		
		// Map Items to BrowserMenu
		navigationBar.getItems().getItems().forEach(i ->
				browserMenus.add(mapToBrowserMenu(i, "item", null)));
		
		// Map Groups to BrowserMenu
		navigationBar.getItems().getGroups().forEach(g -> {
			List<BrowserMenu> children = g.getItems().stream()
					.map(i -> mapToBrowserMenu(i, "item", null))
					.collect(Collectors.toCollection(ArrayList::new));
			
			browserMenus.add(mapToBrowserMenu(g, "group", children));
		});
		
		viewRegister = ViewConfig.createRegister();
	}
	
	private BrowserMenu mapToBrowserMenu(Object source, String type, List<BrowserMenu> children) {
		// Hilfsmethode um DRY zu halten (Don't Repeat Yourself)
		if (source instanceof Item i) {
			return new BrowserMenu(i.getId(), i.getLabel(), i.getColor(), i.getOrder(), type, null, children);
		} else if (source instanceof Group g) {
			return new BrowserMenu(g.getId(), g.getLabel(), g.getColor(), g.getOrder(), type, null, children);
		}
		return null;
	}
	
	@Override
	public void validate() {
		if (!enabled) return;
		
		// Alle Factories laden
		for (IntraConnectViewFactory factory : viewRegister.getFactories()) {
			viewItems.addAll(factory.create().getViews());
		}
		
		// Map für schnellen Zugriff erstellen (Name -> View)
		Map<String, MenuItem> viewMap = viewItems.stream()
				.collect(Collectors.toMap(MenuItem::getId, v -> v, (existing, replacement) -> existing));
		
		// Validierung und Typ-Zuweisung in einem Rutsch
		validateAndEnrichMenus(browserMenus, viewMap);
	}
	
	private void validateAndEnrichMenus(List<BrowserMenu> menus, Map<String, MenuItem> viewMap) {
		for (BrowserMenu menu : menus) {
			String viewID = menu.getId();
			
			if ("group".equals(menu.getMenuTyp())) {
				if (menu.getChildren() != null) {
					validateAndEnrichMenus(menu.getChildren(), viewMap);
				}
			} else {
				// Prüfen ob View existiert
				MenuItem matchedView = viewMap.get(viewID);
				if (matchedView == null) {
					throw new RuntimeException("Kein View mit der ID: '" + viewID + "' gefunden.");
				}
				// ViewTyp direkt setzen
				menu.setViewTyp(matchedView.getView().getType());
			}
		}
	}
	
	@Override
	public void register() {
		//Menus
		buildOrderBrowserMenus();
	}
	
	private void buildOrderBrowserMenus() {
		Comparator<BrowserMenu> orderComparator = Comparator.comparingLong(BrowserMenu::getOrder);
		
		browserMenus.forEach(menu -> {
			if (menu.getChildren() != null) {
				menu.getChildren().sort(orderComparator);
			}
		});
		browserMenus.sort(orderComparator);
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
