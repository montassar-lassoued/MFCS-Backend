package com.IntraConnect.listViews.viewBuilder.dashboard;

import com.IntraConnect.listViews.ViewsType;
import com.IntraConnect.listViews.viewBuilder.builder.AbstractIntraConnectViewBuilder;
import com.IntraConnect.listViews.viewBuilder.builder.IntraConnectViewDetails;

public class DashboardViewBuilder extends AbstractIntraConnectViewBuilder<DashboardViewBuilder> {

    protected DashboardViewBuilder() {
        super(ViewsType.Dashbord);
    }
	
	
	@Override
	public IntraConnectViewDetails build() {
		return new DashboardViewDetail(viewButtons);
	}
}
