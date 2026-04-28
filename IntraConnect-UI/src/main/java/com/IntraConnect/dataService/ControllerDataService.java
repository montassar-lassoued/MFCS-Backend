package com.IntraConnect.dataService;

import com.IntraConnect.UI.MenuItem;
import com.IntraConnect.listViews.ViewsType;
import com.IntraConnect.listViews.viewBuilder.builder.IntraConnectTableDetails;
import com.IntraConnect.listViews.viewBuilder.visualization.VisualizationViewBuilder;
import com.IntraConnect.queryExec.transaction.Transaction;
import com.IntraConnect.listViews.FieldMeta;
import com.IntraConnect.listViews.UIButton;
import com.IntraConnect.listViews.fieldType.Field;
import com.IntraConnect.listViews.viewBuilder.builder.IntraConnectViewDetails;
import com.IntraConnect.visualization.VisuConverterService;
import com.IntraConnect.visualization.VisuData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

@Service
public class ControllerDataService {

    @Autowired
    private List<BrowserMenu> BrowserMenus;

    @Autowired
    List<MenuItem> viewItems;
	@Autowired
	private VisualizationService visualizationService;

    public List<BrowserMenu> getBrowserMenus() {
        return BrowserMenus;
    }

    public Map<String, Object> getViewData(String menu) {
		
		// 1. Hole das MenuItem und die Details (Buttons, Typ etc.)
		MenuItem menuItem = viewItems.stream()
				.filter(vi -> vi.getId().equalsIgnoreCase(menu))
				.findFirst()
				.orElseThrow(() -> new NoSuchElementException("Menu nicht gefunden: " + menu));
		
		Map<String, Object> viewData = new LinkedHashMap<>();
		IntraConnectViewDetails details = menuItem.getView();
		
		if(details.getType().equals(ViewsType.Visualization)) {
			// Die Daten der Visu als VisuData-Objekt holen
			viewData.put("visu", visualizationService.getVisuElement(menu));
			
		} else if (details instanceof IntraConnectTableDetails tableDetails) {
			// Datenbank query holen
			String query = tableDetails.getQuery();
			if (query != null &&!query.isBlank()) {
				
				QueryResult result = executeQuery(query);
				
				viewData.put("rows", result.rows());
				viewData.put("meta", result.meta());
				viewData.put("detailsActions", tableDetails.buildDetailsButtons());
				
				// Metadaten im Objekt speichern
				tableDetails.setMetadata(result.meta());
			}
			
		}
		List<UIButton> viewButtons = getViewButtons(menu);
		viewData.put("viewActions", viewButtons);
		
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			// Macht das JSON schön lesbar (mit Einrückungen)
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			
			String json = mapper.writeValueAsString(viewData);
			System.out.println("--- DEBUG VIEWDATA JSON ---");
			System.out.println(json);
			System.out.println("---------------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return viewData;
    }
	
	private QueryResult executeQuery(String query) {
		try (Transaction transaction = Transaction.create()) {
			ResultSet rs = transaction.select(query);
			ResultSetMetaData rsmd = rs.getMetaData();
			
			// Metadaten einmalig extrahieren
			List<FieldMeta> meta = extractMeta(rsmd);
			// Daten extrahieren
			List<Map<String, Object>> rows = extractRows(rs, rsmd);
			
			return new QueryResult(rows, meta);
		} catch (Exception e) {
			throw new RuntimeException("Fehler bei SQL-Ausführung", e);
		}
	}
	
	private List<FieldMeta> extractMeta(ResultSetMetaData rsmd) throws SQLException {
		List<FieldMeta> metaList = new ArrayList<>();
		int columnCount = rsmd.getColumnCount();
		
		for (int i = 1; i <= columnCount; i++) {
			String colName = rsmd.getColumnName(i);
			String typeName = rsmd.getColumnTypeName(i).toLowerCase();
			
			Field fieldType = mapSqlTypeToField(typeName);
			boolean isNullable = rsmd.isNullable(i) == ResultSetMetaData.columnNullable;
			
			metaList.add(FieldMeta.of(colName)
					.editable(true)
					.visible(!colName.equalsIgnoreCase("id"))
					.nullable(isNullable)
					.type(fieldType));
		}
		return metaList;
	}
	
	private List<Map<String, Object>> extractRows(ResultSet rs, ResultSetMetaData rsmd) throws SQLException {
		List<Map<String, Object>> rows = new ArrayList<>();
		int columnCount = rsmd.getColumnCount();
		
		while (rs.next()) {
			Map<String, Object> row = new LinkedHashMap<>();
			for (int i = 1; i <= columnCount; i++) {
				row.put(rsmd.getColumnName(i), rs.getObject(i));
			}
			rows.add(row);
		}
		return rows;
	}
	
	private Field mapSqlTypeToField(String typeName) {
		return switch (typeName) {
			case "int", "bigint", "decimal" -> Field.NUMBER;
			case "bit", "boolean" -> Field.CHECKBOX;
			case "text", "ntext", "varchar", "nvarchar" -> Field.TEXT_AREA;
			case "date" -> Field.DATE;
			case "datetime", "datetime2", "timestamp" -> Field.DATETIME;
			case "time" -> Field.TIME;
			case "float", "real", "double" -> Field.NUMBER_ANY;
			default -> Field.TEXT_FIELD;
		};
	}
	
	// Hilfs-Record für den internen Datentransfer
	private record QueryResult(List<Map<String, Object>> rows, List<FieldMeta> meta) {}
	
    private List<UIButton> getDetailsButtons(String name) {
		Optional<MenuItem> menu = viewItems.stream()
				.filter(vi -> vi.getId().equalsIgnoreCase(name))
				.findFirst();
		
		if (menu.isPresent()) {
			IntraConnectViewDetails details = menu.get().getView();
			// Nur Table/Cards haben Details-Buttons (Zeilen-Aktionen)
			if (details instanceof IntraConnectTableDetails tableDetails) {
				return tableDetails.buildDetailsButtons();
			}
		}
		return null;
    }

    private List<UIButton> getViewButtons(String name) {
        Optional<MenuItem> menu = viewItems.stream().filter(vi -> vi.getId().equalsIgnoreCase(name)).findFirst();
        return menu.map(menuItem -> menuItem.getView().buildMainButtons()).orElse(null);
    }
}
