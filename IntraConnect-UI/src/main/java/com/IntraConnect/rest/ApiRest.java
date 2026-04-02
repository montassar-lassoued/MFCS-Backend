package com.IntraConnect.rest;

import com.IntraConnect.dataService.BrowserMenu;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/home")
public interface ApiRest {

    @GetMapping(path = "/menu")
    public  List<BrowserMenu> home();

    @GetMapping(path = "/menu/{name}")
    public Map<String, Object> menu(@PathVariable String name);
}
