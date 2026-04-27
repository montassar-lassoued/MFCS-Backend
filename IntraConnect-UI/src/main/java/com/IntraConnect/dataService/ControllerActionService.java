package com.IntraConnect.dataService;

import com.IntraConnect.UI.MenuItem;
import com.IntraConnect.listViews.viewBuilder.builder.IntraConnectTableDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.IntraConnect.queryExec.transaction.Transaction;
import com.IntraConnect.viewCommand.ViewCommand;
import com.IntraConnect.viewCommand.ViewListCommand;
import com.IntraConnect.intf.IntraConnectServiceRequest;
import com.IntraConnect.listViews.Buttons;
import com.IntraConnect.listViews.FieldMeta;
import com.IntraConnect.listViews.UIButton;
import com.IntraConnect.listViews.ViewButton;
import com.IntraConnect.listViews.actionServices.IntraConnectServiceMultiRequest;
import com.IntraConnect.listViews.actionServices.IntraConnectServiceSingleRequest;
import com.IntraConnect.listViews.viewBuilder.builder.IntraConnectViewDetails;
import com.IntraConnect.listViews.viewBuilder.builder.View;
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
	private List<MenuItem> viewItems;
	
	// Wir nutzen einen zentralen ObjectMapper als Bean (Best Practice in Spring)
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	public Object getViewData(ViewCommand cmd) {
		return switch (cmd.action().getId()) {
			case "CREATE", "OPEN", "EDIT" -> getRequestMetaData(cmd);
			default -> throw new IllegalArgumentException("Unsupported action for metadata: " + cmd.action().getId());
		};
	}
	
	public Object handleSingleRequest(ViewCommand cmd) {
		validateAction(cmd.action().getId());
		IntraConnectServiceRequest<?> request = getIntraConnectRequest(cmd.menu(), cmd.action());
		
		if (request instanceof IntraConnectServiceSingleRequest singleRequest) {
			return singleRequest.handle(cmd.payload());
		}
		return null;
	}
	
	public Object handleListRequest(ViewListCommand cmd) {
		validateAction(cmd.action().getId());
		IntraConnectServiceRequest<?> request = getIntraConnectRequest(cmd.menu(), cmd.action());
		
		if (request instanceof IntraConnectServiceMultiRequest multiRequest) {
			return multiRequest.handle(cmd.payload());
		}
		return null;
	}
	
	private void validateAction(String actionId) {
		if (!Set.of("CREATE", "DELETE", "EDIT", "DISCONNECT", "CONNECT", "CUSTOM").contains(actionId)) {
			throw new IllegalArgumentException("Unknown action: " + actionId);
		}
	}
	
	//*************************
	//    META-DATA
	//*************************
	private Object getRequestMetaData(ViewCommand cmd) {
		IntraConnectViewDetails viewDetails = getIntraConnectView(cmd.menu());
		IntraConnectServiceRequest<?> request = getIntraConnectRequest(viewDetails, cmd.action());
		
		if (request == null) return null;
		
		List<FieldMeta> fields = request.viewData();
		// Fallback auf Tabellen-Metadaten
		if (fields == null && viewDetails instanceof IntraConnectTableDetails tableDetails) {
			fields = tableDetails.getMetadata();
		}
		
		return buildFieldStructure(fields);
	}
	
	//************************
	// PILOT-REQUEST LOGIK
	//************************
	private IntraConnectServiceRequest<?> getIntraConnectRequest(String menu, UIButton action) {
		return getIntraConnectRequest(getIntraConnectView(menu), action);
	}
	
	private IntraConnectServiceRequest<?> getIntraConnectRequest(IntraConnectViewDetails details, UIButton action) {
		if (details == null) return null;
		
		Buttons buttonType = Buttons.valueOf(action.getId());
		Optional<ViewButton> target;
		
		if (View.MAIN.equals(action.getActionType())) {
			target = details.getMainButtons().stream()
					.filter(db -> db.button().equals(buttonType))
					.findFirst();
		} else if (View.DETAILS.equals(action.getActionType()) && details instanceof IntraConnectTableDetails tableDetails) {
			target = tableDetails.getDetailsButtons().stream()
					.filter(db -> db.button().equals(buttonType))
					.findFirst();
		} else {
			return null;
		}
		
		return target.map(ViewButton::requestService).orElse(null);
	}
	
	private IntraConnectViewDetails getIntraConnectView(String menuName) {
		return viewItems.stream()
				.filter(vi -> vi.getId().equalsIgnoreCase(menuName))
				.findFirst()
				.map(MenuItem::getView)
				.orElseThrow(() -> new NoSuchElementException("Menu nicht gefunden: " + menuName));
	}
	
	private List<Map<String, Object>> buildFieldStructure(List<FieldMeta> fieldMetas) {
		if (fieldMetas == null) return Collections.emptyList();
		
		List<Map<String, Object>> structure = new ArrayList<>();
		
		for (FieldMeta meta : fieldMetas) {
			Map<String, Object> fieldMap = new LinkedHashMap<>();
			fieldMap.put("field", meta.Of());
			
			Map<String, Object> metaMap = new LinkedHashMap<>();
			metaMap.put("type", meta.getType());
			metaMap.put("nullable", meta.isNullable());
			metaMap.put("visible", meta.isVisible());
			metaMap.put("editable", meta.isEditable());
			metaMap.put("defaultValue", Objects.requireNonNullElse(meta.getDefaultValue(), ""));
			
			fieldMap.put("meta", metaMap);
			
			if (meta.getQuery() != null && !meta.getQuery().isBlank()) {
				fieldMap.put("values", fetchLookupValues(meta.getQuery()));
			}

			structure.add(fieldMap);
		}
		return structure;
	}
	
	private List<Object> fetchLookupValues(String query) {
		List<Object> values = new ArrayList<>();
		try (Transaction transaction = Transaction.create()) {
			ResultSet rs = transaction.select(query);
			int cols = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				// Wenn nur eine Spalte, direkt Wert hinzufügen, sonst Array/Map
				if (cols == 1) {
					values.add(rs.getObject(1));
				} else {
					List<Object> row = new ArrayList<>();
					for (int i = 1; i <= cols; i++) {
						row.add(rs.getObject(i));
					}
					values.add(row);
				}
			}
		} catch (Exception e) {
			log.error("Fehler beim Laden der Lookup-Werte für Query: {}", query, e);
		}
		return values;
	}
}
