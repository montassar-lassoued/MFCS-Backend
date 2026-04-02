package com.IntraConnect.listViews.viewBuilder;

import com.IntraConnect.listViews.Buttons;
import com.IntraConnect.listViews.ViewButton;
import com.IntraConnect.listViews.ViewsType;
import com.IntraConnect.listViews.actionServices.PilotServiceSingleRequest;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPilotDetailsViewBuilder
        extends AbstractPilotViewBuilder<AbstractPilotDetailsViewBuilder> {

    private final List<ViewButton> detailsButtons = new ArrayList<>();

    public AbstractPilotDetailsViewBuilder(ViewsType type) {
        super(type);
    }

    public AbstractPilotDetailsViewBuilder addNewDetailsButton(String label, PilotServiceSingleRequest newRequest) {
        detailsButtons.add(new ViewButton(Buttons.CREATE, label, newRequest));
        return this;
    }

    public AbstractPilotDetailsViewBuilder addEditDetailsButton(String label,PilotServiceSingleRequest editRequest) {
        detailsButtons.add(new ViewButton(Buttons.EDIT, label, editRequest));
        return this;
    }

    public AbstractPilotDetailsViewBuilder addOpenDetailsButton(String label, PilotServiceSingleRequest pilotViewRequest) {
        detailsButtons.add(new ViewButton(Buttons.OPEN, label, pilotViewRequest));
        return this;
    }

    public AbstractPilotDetailsViewBuilder addDeleteDetailsButton(String label,PilotServiceSingleRequest deleteRequest) {
        detailsButtons.add(new ViewButton(Buttons.DELETE, label, deleteRequest));
        return this;
    }

    public PilotViewDetails build() {
        return buildInternal(detailsButtons);
    }
}
