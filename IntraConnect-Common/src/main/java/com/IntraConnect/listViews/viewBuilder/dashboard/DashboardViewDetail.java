package com.IntraConnect.listViews.viewBuilder.dashboard;

import com.IntraConnect.listViews.ViewButton;
import com.IntraConnect.listViews.ViewsType;
import com.IntraConnect.listViews.viewBuilder.builder.IntraConnectViewDetails;

import java.util.List;

public class DashboardViewDetail extends IntraConnectViewDetails {
	
	protected DashboardViewDetail( List<ViewButton> mainButtons) {
		super(ViewsType.Dashbord, mainButtons);
	}
}
