package com.IntraConnect.listViews.viewBuilder.builder;

import com.IntraConnect.listViews.UIButton;
import com.IntraConnect.listViews.ViewButton;
import com.IntraConnect.listViews.ViewsType;
import com.IntraConnect.listViews.viewBuilder.card.CardViewBuilder;
import com.IntraConnect.listViews.viewBuilder.table.TableViewBuilder;
import com.IntraConnect.listViews.viewBuilder.visualization.VisualizationViewBuilder;

import java.util.List;

public class IntraConnectViewDetails {
	
    private final ViewsType type;
    private final List<ViewButton> mainButtons;


    protected IntraConnectViewDetails(
            ViewsType type,
            List<ViewButton> mainButtons
 
    ) {
		this.type = type;
        this.mainButtons = List.copyOf(mainButtons);
    }

    // ========= Factory =========

    public static CardViewBuilder cardView() {
        return new CardViewBuilder();
    }

    public static TableViewBuilder tableView() {
        return new TableViewBuilder();
    }
	
	public static VisualizationViewBuilder visualizationView() {
		return new VisualizationViewBuilder();
	}
	public List<ViewButton> getMainButtons() {
		return mainButtons;
	}
	
	public List<UIButton> buildMainButtons() {
        return IntraConnectViewButtonFactory.view(mainButtons);
    }
	
	public ViewsType getType() { return type; }
}
