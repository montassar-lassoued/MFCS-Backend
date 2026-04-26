package com.IntraConnect.listViews.viewBuilder.builder;

import com.IntraConnect.listViews.Buttons;
import com.IntraConnect.listViews.ViewButton;
import com.IntraConnect.listViews.ViewsType;
import com.IntraConnect.listViews.actionServices.IntraConnectServiceSingleRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractIntraConnectDetailsViewBuilder
        extends AbstractIntraConnectViewBuilder<AbstractIntraConnectDetailsViewBuilder> {

    private final List<ViewButton> detailsButtons = new ArrayList<>();
	protected String query;

    public AbstractIntraConnectDetailsViewBuilder(ViewsType type) {
        super(type);
    }
	
	public AbstractIntraConnectDetailsViewBuilder query(String query) {
		this.query = query.toUpperCase();
		return this;
	}
	
    public AbstractIntraConnectDetailsViewBuilder addNewDetailsButton(String label, IntraConnectServiceSingleRequest newRequest) {
        detailsButtons.add(new ViewButton(Buttons.CREATE, label, newRequest));
        return this;
    }

    public AbstractIntraConnectDetailsViewBuilder addEditDetailsButton(String label,IntraConnectServiceSingleRequest editRequest) {
        detailsButtons.add(new ViewButton(Buttons.EDIT, label, editRequest));
        return this;
    }

    public AbstractIntraConnectDetailsViewBuilder addOpenDetailsButton(String label, IntraConnectServiceSingleRequest pilotViewRequest) {
        detailsButtons.add(new ViewButton(Buttons.OPEN, label, pilotViewRequest));
        return this;
    }

    public AbstractIntraConnectDetailsViewBuilder addDeleteDetailsButton(String label,IntraConnectServiceSingleRequest deleteRequest) {
        detailsButtons.add(new ViewButton(Buttons.DELETE, label, deleteRequest));
        return this;
    }

    public IntraConnectTableDetails build() {
		Pattern p = Pattern.compile(
				"(?i)([A-Z0-9_]+)\\.([A-Z0-9_]+)(?:\\s+AS\\s+([A-Z0-9_]+))?"
		);
		
		Matcher m = p.matcher(query);
		TableColumnAliasMapper mapper = new TableColumnAliasMapper();
		
		while (m.find()) {
			mapper.setTableName(m.group(1));
			String realCol = m.group(2);
			String alias = m.group(3) != null ? m.group(3) : realCol;
			mapper.put(alias, realCol);
		}
		
		return new IntraConnectTableDetails(
				query,
				type,
				viewButtons,
				detailsButtons,
				mapper
		);
    }
}
