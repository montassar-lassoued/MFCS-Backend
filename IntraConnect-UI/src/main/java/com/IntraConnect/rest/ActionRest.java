package com.IntraConnect.rest;

import com.IntraConnect.viewCommand.ViewCommand;
import com.IntraConnect.viewCommand.ViewListCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(path = "/api/action")
public interface ActionRest {

    @RequestMapping("/single/execute")
    ResponseEntity<?> handleSingleRequest(@RequestBody ViewCommand cmd);

    @RequestMapping("list/execute")
    ResponseEntity<?> handleListRequests(@RequestBody ViewListCommand cmd);

    @RequestMapping("/details")
    ResponseEntity<?> getViewData(@RequestBody ViewCommand cmd);
}
