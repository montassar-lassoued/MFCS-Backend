package com.IntraConnect.listViews.viewBuilder;

import com.IntraConnect.listViews.ViewsType;

import java.util.List;

public class DashboardViewBuilder extends AbstractPilotViewBuilder<DashboardViewBuilder> {

    protected DashboardViewBuilder() {
        super(ViewsType.Dashbord);
    }

    public PilotViewDetails build() {
        return buildInternal(List.of());
    }
}
