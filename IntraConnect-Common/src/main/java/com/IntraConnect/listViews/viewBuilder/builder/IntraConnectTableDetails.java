package com.IntraConnect.listViews.viewBuilder.builder;

import com.IntraConnect.listViews.FieldMeta;
import com.IntraConnect.listViews.UIButton;
import com.IntraConnect.listViews.ViewButton;
import com.IntraConnect.listViews.ViewsType;

import java.util.ArrayList;
import java.util.List;

public class IntraConnectTableDetails extends IntraConnectViewDetails {
	private final String query;
	private final List<ViewButton> detailsButtons;
	private final TableColumnAliasMapper columnMap;
	private List<FieldMeta> metadata = new ArrayList<>();
	
	public IntraConnectTableDetails(String query, ViewsType type, List<ViewButton> mainButtons,
							 List<ViewButton> detailsButtons, TableColumnAliasMapper columnMap) {
		super(type, mainButtons);
		this.query = query;
		this.detailsButtons = List.copyOf(detailsButtons);
		this.columnMap = columnMap;
	}
	
	public String getQuery() {
		return query;
	}
	
	public List<UIButton> buildDetailsButtons() {
		return IntraConnectViewButtonFactory.details(detailsButtons);
	}
	
	// ========= Getter =========
	
	public TableColumnAliasMapper getColumnMap() { return columnMap; }
	public List<ViewButton> getDetailsButtons(){
		return detailsButtons;
	}
	
	public List<FieldMeta> getMetadata() {
		return metadata;
	}
	
	public void setMetadata(List<FieldMeta> metadata) {
		this.metadata = metadata;
	}
}