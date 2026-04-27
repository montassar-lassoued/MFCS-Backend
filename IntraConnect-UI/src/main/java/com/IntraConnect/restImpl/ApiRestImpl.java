package com.IntraConnect.restImpl;


import com.IntraConnect.FrontendBroker.service.VisuPushService;
import com.IntraConnect.UI.MenuItem;
import com.IntraConnect.dataService.BrowserMenu;
import com.IntraConnect.dataService.ControllerDataService;
import com.IntraConnect.rest.ApiRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


@RestController
public class ApiRestImpl implements ApiRest {

    @Autowired
    private ControllerDataService controllerDataService;
	@Autowired
	private VisuPushService visuPushService;
    @Autowired
    List<MenuItem> viewItems;

    @Override
    public  List<BrowserMenu> home() {
        return  controllerDataService.getBrowserMenus();
    }

    @Override
    public Map<String, Object> menu(String name) {
		/*if(name.equals("visualization")) {
		// Beispiel LUs im Frontend zu bewegen
		
			visuPushService.publishLuMovement("132", "S001", "LEFT_RECT", "S");
			visuPushService.publishLuMovement("133", "S003", "LEFT_RECT", "L");
			visuPushService.publishLuMovement("320", "S201", "LEFT_RECT", "R");
			visuPushService.publishLuMovement("321", "S201", "LEFT_RECT", "L");
			visuPushService.publishLuMovement("322", "S201", "LEFT_RECT", "S");
		}*/
        return controllerDataService.getViewData(name);
    }
}
