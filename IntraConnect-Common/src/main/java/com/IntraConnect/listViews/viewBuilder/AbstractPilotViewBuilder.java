package com.IntraConnect.listViews.viewBuilder;

import com.IntraConnect.intf.PilotServiceRequest;
import com.IntraConnect.listViews.Buttons;
import com.IntraConnect.listViews.ViewButton;
import com.IntraConnect.listViews.ViewsType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractPilotViewBuilder <T extends AbstractPilotViewBuilder<T>>{
    protected final ViewsType type;
    protected String query;
    protected List<ViewButton> viewButtons = new ArrayList<>();

    protected AbstractPilotViewBuilder(ViewsType type) {
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

    public T query(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalStateException("Query can't be empty");
        }
        this.query = query.toUpperCase();
        return self();
    }

    public T addViewButton(Buttons button, String label, PilotServiceRequest requestService) {
        viewButtons.add(new ViewButton(button,label, requestService));
        return self();
    }

    protected PilotViewDetails buildInternal(List<ViewButton> detailsButtons) {

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

        return new PilotViewDetails(
                query,
                type,
                viewButtons,
                detailsButtons,
                mapper
        );
    }
}
