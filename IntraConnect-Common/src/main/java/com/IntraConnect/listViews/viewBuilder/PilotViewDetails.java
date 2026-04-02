package com.IntraConnect.listViews.viewBuilder;

import com.IntraConnect.listViews.FieldMeta;
import com.IntraConnect.listViews.UIButton;
import com.IntraConnect.listViews.ViewButton;
import com.IntraConnect.listViews.ViewsType;

import java.util.ArrayList;
import java.util.List;

public class PilotViewDetails {

    private final String query;
    private final ViewsType type;
    private final List<ViewButton> detailsButtons;
    private final List<ViewButton> mainButtons;
    private final TableColumnAliasMapper columnMap;
    private List<FieldMeta> metadata = new ArrayList<>();

    PilotViewDetails(
            String query,
            ViewsType type,
            List<ViewButton> mainButtons,
            List<ViewButton> detailsButtons,
            TableColumnAliasMapper columnMap
    ) {
        this.query = query;
        this.type = type;
        this.mainButtons = List.copyOf(mainButtons);
        this.detailsButtons = List.copyOf(detailsButtons);
        this.columnMap = columnMap;
    }

    // ========= Factory =========

    public static CardViewBuilder cardView() {
        return new CardViewBuilder();
    }

    public static TableViewBuilder tableView() {
        return new TableViewBuilder();
    }

    // ========= Public API =========

    public List<UIButton> buildDetailsButtons() {
        return PilotViewButtonFactory.details(detailsButtons);
    }

    public List<UIButton> buildMainButtons() {
        return PilotViewButtonFactory.view(mainButtons);
    }

    // ========= Getter =========

    public String getQuery() { return query; }
    public ViewsType getType() { return type; }
    public TableColumnAliasMapper getColumnMap() { return columnMap; }
    public List<ViewButton> getDetailsButtons(){
        return detailsButtons;
    }
    public List<ViewButton> getMainButtons(){
        return mainButtons;
    }

    public List<FieldMeta> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<FieldMeta> metadata) {
        this.metadata = metadata;
    }
}
