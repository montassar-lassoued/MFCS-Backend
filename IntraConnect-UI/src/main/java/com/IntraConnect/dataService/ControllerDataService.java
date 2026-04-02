package com.IntraConnect.dataService;

import com.IntraConnect.UI.MenuItem;
import com.IntraConnect.queryExec.transaction.Transaction;
import com.IntraConnect.listViews.FieldMeta;
import com.IntraConnect.listViews.UIButton;
import com.IntraConnect.listViews.fieldType.Field;
import com.IntraConnect.listViews.viewBuilder.PilotViewDetails;
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
    private List<FieldMeta> metadata_g = new ArrayList<>();

    public List<BrowserMenu> getBrowserMenus() {
        return BrowserMenus;
    }

    public Map<String, Object> getViewData(String menu) {
        String query = getQueryFromMap(menu);
        if (query.isBlank()) {
            return new LinkedHashMap<>();
        }
        Map<String, Object> viewData = new LinkedHashMap<>();
        List<Map<String, Object>> data = getData(query, menu);
        List<UIButton> viewButtons = getViewButtons(menu);
        viewData.put("rows", data);
        viewData.put("meta", metadata_g);
        viewData.put("detailsActions", getDetailsButtons(menu));
        viewData.put("viewActions", viewButtons);

        return viewData;
    }

    private List<Map<String, Object>> getData(String query, String menu) {

        try (Transaction transaction = Transaction.create()) {
            ResultSet rs = transaction.select(query);
            // Meta-Daten -> Merken
            saveViewMetaData(menu, rs);
            // Daten holen, falls keine dann nur die
            // Columns, dass die Views nicht leer bleiben
            return data(rs, menu);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Map<String, Object>> data(ResultSet rs, String menu) throws SQLException {
       List<Map<String, Object>> rows = new ArrayList<>();
        int cols = rs.getMetaData().getColumnCount();
        // dass die erste Zeile nicht übersprungen wird
        boolean hasRow = rs.next();
        if(!hasRow){
            return null;
        }
        do {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= cols; i++) {
                String columnName = rs.getMetaData().getColumnName(i);
                Object value = rs.getObject(i);

                row.put(columnName, value);
            }
            rows.add(row);

        } while (rs.next());

        return rows;
    }

    private void saveViewMetaData(String name, ResultSet rs) throws SQLException {
        Optional<MenuItem> menu = viewItems.stream().filter(vi -> vi.getName().equalsIgnoreCase(name)).findFirst();
        if (menu.isPresent()) {
            PilotViewDetails viewDetails = menu.get().getView();
            List<FieldMeta> metadata = getViewMetaData(rs);
            viewDetails.setMetadata(metadata);
            metadata_g = metadata;
        }
    }

    private List<FieldMeta> getViewMetaData(ResultSet rs) throws SQLException {
        List<FieldMeta> metadaten= new ArrayList<>();
        int cols = rs.getMetaData().getColumnCount();
        for (int i = 1; i <= cols; i++) {
            String columnName = rs.getMetaData().getColumnName(i);
            boolean nullable = rs.getMetaData().isNullable(i) == ResultSetMetaData.columnNullable;
            boolean visible = !columnName.equalsIgnoreCase("id");
            Field columnType = switch (rs.getMetaData().getColumnTypeName(i)) {
                case "int", "bigint", "decimal" -> Field.NUMBER;
                case "bit" -> Field.CHECKBOX;
                case "text", "ntext" -> Field.TEXT_AREA;
                case "date" -> Field.DATE;
                case "datetime", "datetime2" -> Field.DATETIME;
                case "time" -> Field.TIME;
                case "float", "real" -> Field.NUMBER_ANY;
                default -> Field.TEXT_FIELD;
            };
            //**Für Metadaten
            metadaten.add(
                    FieldMeta.of(columnName)
                            .editable(true)
                            .visible(visible)
                            .nullable(nullable)
                            .type(columnType)
            );
        }
        return metadaten;
    }

    private String getQueryFromMap(String name) {
        Optional<MenuItem> menu = viewItems.stream().filter(vi -> vi.getName().equalsIgnoreCase(name)).findFirst();
        if (menu.isPresent()) {
            return menu.get().getView().getQuery();
        }
        return "";
    }

    private List<UIButton> getDetailsButtons(String name) {
        Optional<MenuItem> menu = viewItems.stream().filter(vi -> vi.getName().equalsIgnoreCase(name)).findFirst();
        return menu.map(menuItem -> menuItem.getView().buildDetailsButtons()).orElse(null);
    }

    private List<UIButton> getViewButtons(String name) {
        Optional<MenuItem> menu = viewItems.stream().filter(vi -> vi.getName().equalsIgnoreCase(name)).findFirst();
        return menu.map(menuItem -> menuItem.getView().buildMainButtons()).orElse(null);
    }
}
