package com.IntraConnect.restImpl;


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
    List<MenuItem> viewItems;

    @Override
    public  List<BrowserMenu> home() {
        return  controllerDataService.getBrowserMenus();
    }

    @Override
    public Map<String, Object> menu(String name) {
        return controllerDataService.getViewData(name);
    }
}
