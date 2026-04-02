package com.IntraConnect.rest;

import com.IntraConnect.viewCommand.ViewCommand;
import com.IntraConnect.viewCommand.ViewListCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(path = "/api/action")
public interface ActionRest {

    @RequestMapping(path = "/single/execute")
    ResponseEntity<?> handleSingleRequest(@RequestBody ViewCommand cmd);

    @RequestMapping(path = "list/execute")
    ResponseEntity<?> handleListRequests(@RequestBody ViewListCommand cmd);

    @RequestMapping(path = "/Details")
    ResponseEntity<?> getViewData(@RequestBody ViewCommand cmd);
}
