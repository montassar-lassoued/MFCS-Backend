package com.IntraConnect.listViews.viewBuilder.visualization;

import com.IntraConnect.listViews.ViewsType;
import com.IntraConnect.listViews.viewBuilder.builder.AbstractIntraConnectViewBuilder;
import com.IntraConnect.listViews.viewBuilder.builder.IntraConnectVisualizationDetails;

public class VisualizationViewBuilder extends AbstractIntraConnectViewBuilder<VisualizationViewBuilder> {
	
	public VisualizationViewBuilder() {
		super(ViewsType.Visualization);
	}
	
	@Override
	public IntraConnectVisualizationDetails build() {
		// Erzeugt das spezifische Visu-Objekt ohne SQL-Logik
		return new IntraConnectVisualizationDetails(viewButtons);
	}
	
}
