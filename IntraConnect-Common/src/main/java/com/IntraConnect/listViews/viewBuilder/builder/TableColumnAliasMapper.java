package com.IntraConnect.listViews.viewBuilder.builder;

import java.util.LinkedHashMap;
import java.util.Map;

public class TableColumnAliasMapper {

    private String tableName;
    private final Map<String, String> columnMap = new LinkedHashMap<>();

    public void put(String alias, String realCol){
        this.columnMap.put(alias, realCol);
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return this.tableName ;
    }

    public Map<String, String> getColumnMap() {
        return this.columnMap;
    }
}
