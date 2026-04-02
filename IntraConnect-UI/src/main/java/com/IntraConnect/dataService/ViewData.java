package com.IntraConnect.dataService;

import com.IntraConnect.listViews.UIButton;

import java.util.List;

public record ViewData(List<RowData> rows, List<UIButton> action) {

}
