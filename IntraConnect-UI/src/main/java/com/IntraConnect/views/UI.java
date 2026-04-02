package com.IntraConnect.views;

import com.IntraConnect.listViews.Buttons;
import com.IntraConnect.listViews.PilotView;
import com.IntraConnect.views.requests.UserAddRequest;
import com.IntraConnect.views.requests.UserEditRequest;
import com.IntraConnect.intf.PilotViewFactory;
import com.IntraConnect.UI.MenuItem;
import com.IntraConnect.listViews.viewBuilder.PilotViewDetails;

import java.util.List;

public class UI extends PilotView {

    @Override
    public PilotViewFactory getClassType() {
        return UI::new;
    }

    @Override
    public List<MenuItem> listViews() {

        List<MenuItem> items = List.of(
                new MenuItem("Users", createCardsUser()),
                new MenuItem("Home", createHomePage())

        );
        return items;
    }

    private PilotViewDetails createCardsUser() {
        String query ="SELECT APPUSERS.ID, " +
                "APPUSERS.USERNAME as Name, " +
                "APPUSERS.EMAIL as EMail, " +
                "ROLE.ROLE as Rolle, " +
                "APPUSERS.STATE as Status " +
                "FROM APPUSERS " +
                "LEFT JOIN ROLE ON (APPUSERS.ROLE_ID = ROLE.ID)";


        return PilotViewDetails.cardView()
                .query(query)
                .addEditDetailsButton("",new UserEditRequest())
                .addViewButton(Buttons.CREATE,"add", new UserAddRequest())
                //.addViewButton(Buttons.EDIT,"edit", new UserEditRequest())
                .build();

    }
    private PilotViewDetails createHomePage() {
        String query ="SELECT APPUSERS.ID, " +
                "APPUSERS.USERNAME as name, " +
                "APPUSERS.EMAIL, " +
                "ROLE.ROLE, " +
                "APPUSERS.STATE " +
                "FROM APPUSERS " +
                "LEFT JOIN ROLE ON (APPUSERS.ROLE_ID = ROLE.ID)";
        return PilotViewDetails.cardView()
                .query(query)
                .build();
    }
}
