package com.IntraConnect.dataService;

import com.IntraConnect.UI.MenuItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.IntraConnect.queryExec.transaction.Transaction;
import com.IntraConnect.viewCommand.ViewCommand;
import com.IntraConnect.viewCommand.ViewListCommand;
import com.IntraConnect.intf.PilotServiceRequest;
import com.IntraConnect.listViews.Buttons;
import com.IntraConnect.listViews.FieldMeta;
import com.IntraConnect.listViews.UIButton;
import com.IntraConnect.listViews.ViewButton;
import com.IntraConnect.listViews.actionServices.PilotServiceMultiRequest;
import com.IntraConnect.listViews.actionServices.PilotServiceSingleRequest;
import com.IntraConnect.listViews.viewBuilder.PilotViewDetails;
import com.IntraConnect.listViews.viewBuilder.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.*;

@Service
public class ControllerActionService {
	
	private static final Logger log = LoggerFactory.getLogger(ControllerActionService.class);
	@Autowired
	List<MenuItem> viewItems;
	
	
	public Object getViewData(ViewCommand cmd) {
		return switch (cmd.action().getId()) {
			case "CREATE", "OPEN", "EDIT" -> getRequestMetaData(cmd);
			//case "DELETE" -> getDeleteRequestMetaData(cmd); // gibt es nicht
			default -> throw new IllegalArgumentException("Unknown action " + cmd.action());
		};
	}
	
	public Object handleSingleRequest(ViewCommand cmd) {
		return switch (cmd.action().getId()) {
			case "CREATE", "DELETE", "EDIT", "CUSTOM" -> handleRequest(cmd);
			//case "OPEN" -> executeOpenRequest(cmd); // gibt es eigentlich nicht
			default -> throw new IllegalArgumentException("Unknown action " + cmd.action());
		};
	}
	
	public Object handleListRequest(ViewListCommand cmd) {
		return switch (cmd.action().getId()) {
			case "CREATE", "DELETE", "EDIT", "CUSTOM" -> handleRequest(cmd);
			//case "OPEN" -> executeOpenRequest(cmd); // gibt es eigentlich nicht
			default -> throw new IllegalArgumentException("Unknown action " + cmd.action());
		};
	}
	
	//*************************
	//    EXECUTE
	//*************************
	private Object handleRequest(ViewListCommand cmd) {
		PilotViewDetails pilotViewDetails = getPilotView(cmd.menu());
		PilotServiceMultiRequest pilotServiceMultiRequest = (PilotServiceMultiRequest) getPilotRequest(pilotViewDetails, cmd.action());
		if (pilotServiceMultiRequest == null) {
			return null;
		}
		return pilotServiceMultiRequest.handle(cmd.payload());
	}
	
	private Object handleRequest(ViewCommand cmd) {
		PilotViewDetails pilotViewDetails = getPilotView(cmd.menu());
		PilotServiceSingleRequest pilotServiceSingleRequest = (PilotServiceSingleRequest) getPilotRequest(pilotViewDetails, cmd.action());
		if (pilotServiceSingleRequest == null) {
			return null;
		}
		return pilotServiceSingleRequest.handle(cmd.payload());
	}
	
	//*************************
	//    META-DATA
	//*************************
	private Object getRequestMetaData(ViewCommand cmd) {
		PilotViewDetails pilotViewDetails = getPilotView(cmd.menu());
		PilotServiceRequest pilotServiceRequest = getPilotRequest(pilotViewDetails, cmd.action());
		if (pilotServiceRequest == null) {
			return null;
		}
		List<FieldMeta> fields = pilotServiceRequest.viewData();
		if (fields == null) {
			fields = pilotViewDetails.getMetadata();
		}
		return getFieldStruktur(fields);
	}
	
	
	//************************
	// PILOT-REQUEST <Klasse
	// für die Anzeige/Bearbeitung
	// der Daten>
	//************************
	private PilotServiceRequest getPilotRequest(PilotViewDetails pilotViewDetails, UIButton action) {
		assert pilotViewDetails != null;
		Optional<ViewButton> detailsButton;
		if (action.getActionType().equals(View.DETAILS)) {
			detailsButton = pilotViewDetails.getDetailsButtons()
					.stream()
					.filter(db -> db.button().equals(Buttons.valueOf(action.getId())))
					.findFirst();
		} else if (action.getActionType().equals(View.MAIN)) {
			detailsButton = pilotViewDetails.getMainButtons()
					.stream()
					.filter(db -> db.button().equals(Buttons.valueOf(action.getId())))
					.findFirst();
		} else {
			return null;
		}
		
		return detailsButton.map(ViewButton::requestService).orElse(null);
	}
	
	private PilotViewDetails getPilotView(String menu) {
		Optional<MenuItem> item = viewItems.stream().filter(vi -> vi.getName().equalsIgnoreCase(menu)).findFirst();
		return item.map(MenuItem::getView).orElse(null);
	}
	
	private Object getFieldStruktur(List<FieldMeta> fieldMeta) {
		if (fieldMeta == null) {
			return null;
		}
		List<Map<String, Object>> data = new ArrayList<>();
		for (FieldMeta meta : fieldMeta) {
			Map<String, Object> field = new LinkedHashMap<>();
			Map<String, Object> metaData = new LinkedHashMap<>();
			
			field.put("field", meta.Of());
			metaData.put("type", meta.getType());
			metaData.put("nullable", meta.isNullable());
			metaData.put("visible", meta.isVisible());
			metaData.put("editable", meta.isEditable());
			metaData.put("defaultValue", meta.getDefaultValue());
			field.put("meta", metaData);
			
			if (meta.getQuery() != null && !meta.getQuery().isBlank()) {
				try (Transaction transaction = Transaction.create()) {
					ResultSet result = transaction.select(meta.getQuery());
					int cols = result.getMetaData().getColumnCount();
					List<Object> row = new ArrayList<>();
					while (result.next()) {
						
						for (int i = 1; i <= cols; i++) {
							row.add(result.getObject(i));
						}
					}
					field.put("values", row);
					
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
			
			data.add(field);
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(data);
			System.out.println(json);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return data;
	}
	
	
}
