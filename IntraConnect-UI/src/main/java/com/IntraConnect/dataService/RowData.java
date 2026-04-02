package com.IntraConnect.dataService;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.Map;

public record RowData(Map<String, Object> row) {
    @JsonAnyGetter
    public Map<String, Object> any() {
        return row;
    }
}
