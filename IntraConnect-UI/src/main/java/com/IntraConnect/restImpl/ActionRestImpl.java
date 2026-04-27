package com.IntraConnect.restImpl;

import com.IntraConnect._enum.Response;
import com.IntraConnect.dataService.ControllerActionService;
import com.IntraConnect.rest.ActionRest;
import com.IntraConnect.viewCommand.ViewCommand;
import com.IntraConnect.viewCommand.ViewListCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.function.Supplier;

@RestController
public class ActionRestImpl implements ActionRest {
	
	private final ControllerActionService controllerActionService;
	private static final Map<String, String> SUCCESS_RESPONSE = Map.of("status", "OK");
	
	public ActionRestImpl(ControllerActionService controllerActionService) {
		this.controllerActionService = controllerActionService;
	}
	
	@Override
	public ResponseEntity<?> handleSingleRequest(ViewCommand cmd) {
		return execute(()-> controllerActionService.handleSingleRequest(cmd));
	}
	
	@Override
	public ResponseEntity<?> handleListRequests(ViewListCommand cmd) {
		return execute(()-> controllerActionService.handleListRequest(cmd));
	}
	
	@Override
	public ResponseEntity<?> getViewData(ViewCommand cmd) {
		return execute(()-> controllerActionService.getViewData(cmd));
	}
	
	/**
	 *  Zentrale Methode zur Rückmeldungsbehandlung.
	 */
	private ResponseEntity<?> execute(Supplier<Object> action) {
		var result = action.get();
		if(result instanceof Response) {
			return switch (result) {
				case Response.ERROR ->
						ResponseEntity.status(HttpStatus.FORBIDDEN).body("Aktion für diesen Datensatz konnte nicht durchgeführt werden!");
				case Response.INTERNAL_SERVER_ERROR ->
						ResponseEntity.status(HttpStatus.FORBIDDEN).body("Interner Serverfehler!");
				case Response.AUTHORIZATION_ERROR ->
						ResponseEntity.status(HttpStatus.FORBIDDEN).body("Aktion für diesen Datensatz nicht erlaubt!");
				case Response.INCOMPLETE_DATA ->
						ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Die Daten sind unvollständig!");
				case Response.ACTION_NOT_ALLOWED ->
						ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Aktion nicht erlaubt!");
				default -> ResponseEntity.ok(SUCCESS_RESPONSE);
			};
		}
		return ResponseEntity.ok(result);
	}
}
