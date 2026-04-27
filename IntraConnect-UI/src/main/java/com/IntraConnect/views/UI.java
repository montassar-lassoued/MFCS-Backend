package com.IntraConnect.views;

import com.IntraConnect.listViews.Buttons;
import com.IntraConnect.listViews.IntraConnectView;
import com.IntraConnect.views.requests.UserAddRequest;
import com.IntraConnect.views.requests.UserDeleteRequest;
import com.IntraConnect.views.requests.UserEditRequest;
import com.IntraConnect.intf.IntraConnectViewFactory;
import com.IntraConnect.UI.MenuItem;
import com.IntraConnect.listViews.viewBuilder.builder.IntraConnectViewDetails;

import java.util.List;

public class UI extends IntraConnectView {

    @Override
    public IntraConnectViewFactory getClassType() {
        return UI::new;
    }

    @Override
    public List<MenuItem> listViews() {

        List<MenuItem> items = List.of(
                new MenuItem("users", createCardsUser())
                //new MenuItem("home", createHomePage())

        );
        return items;
    }

    private IntraConnectViewDetails createCardsUser() {
        String query ="SELECT APPUSERS.ID, " +
                "APPUSERS.USERNAME as Name, " +
                "APPUSERS.EMAIL as EMail, " +
                "ROLE.ROLE as Rolle, " +
                "APPUSERS.STATE as Status " +
                "FROM APPUSERS " +
                "LEFT JOIN ROLE ON (APPUSERS.ROLE_ID = ROLE.ID)";


        return IntraConnectViewDetails.cardView()
                .query(query)
                .addEditDetailsButton("",new UserEditRequest())
				.addDeleteDetailsButton("", new UserDeleteRequest())
                .addViewButton(Buttons.CREATE,"add user", new UserAddRequest())
				
                //.addViewButton(Buttons.EDIT,"edit", new UserEditRequest())
                .build();

    }
  
}
