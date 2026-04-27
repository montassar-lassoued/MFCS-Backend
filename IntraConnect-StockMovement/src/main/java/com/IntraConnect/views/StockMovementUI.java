package com.IntraConnect.views;

import com.IntraConnect.UI.MenuItem;
import com.IntraConnect.intf.IntraConnectViewFactory;
import com.IntraConnect.listViews.IntraConnectView;
import com.IntraConnect.listViews.viewBuilder.builder.IntraConnectViewDetails;

import java.util.List;

public class StockMovementUI extends IntraConnectView {
	
	@Override
	public List<MenuItem> listViews() {
		List<MenuItem> items = List.of(
				new MenuItem("visualization_1", createVisualization()),
				new MenuItem("visualization_2", createVisualization())
		);
		return items;
	}
	
	private IntraConnectViewDetails createVisualization() {
		// hier versuchen die Visu anhand des Namens zu holen
		// im Build vielleicht, und die Daten werden Automatisch gemerkt
		// für das Frontend
		return IntraConnectViewDetails.visualizationView().build();
	}
	
	@Override
	public IntraConnectViewFactory getClassType() {
		return StockMovementUI::new;
	}
}
