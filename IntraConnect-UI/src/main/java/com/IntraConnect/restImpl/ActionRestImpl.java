package com.IntraConnect.restImpl;

import com.IntraConnect.dataService.ControllerActionService;
import com.IntraConnect.rest.ActionRest;
import com.IntraConnect.viewCommand.ViewCommand;
import com.IntraConnect.viewCommand.ViewListCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ActionRestImpl implements ActionRest {
	
	@Autowired
	private ControllerActionService controllerActionService;
	
	@Override
	public ResponseEntity<?> handleSingleRequest(ViewCommand cmd) {
		var result = controllerActionService.handleSingleRequest(cmd);
		return ResponseEntity.ok(result);
	}
	
	@Override
	public ResponseEntity<?> handleListRequests(ViewListCommand cmd) {
		var result = controllerActionService.handleListRequest(cmd);
		return ResponseEntity.ok(result);
	}
	
	@Override
	public ResponseEntity<?> getViewData(ViewCommand cmd) {
		var result = controllerActionService.getViewData(cmd);
		return ResponseEntity.ok(result);
	}
}
